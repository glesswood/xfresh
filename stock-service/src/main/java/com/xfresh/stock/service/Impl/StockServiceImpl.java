package com.xfresh.stock.service.Impl;

import com.xfresh.order.dto.StockDTO;
import com.xfresh.order.dto.cmd.StockDeductCmd;
import com.xfresh.order.dto.cmd.StockInitCmd;
import com.xfresh.stock.entity.Stock;
import com.xfresh.stock.exception.StockException;
import com.xfresh.stock.mapper.StockMapper;
import com.xfresh.stock.repository.StockRepository;
import com.xfresh.stock.service.StockService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockServiceImpl implements StockService {

    private static final String KEY_PREFIX = "stock:";
    private static final long   LUA_MISS   = -2L;

    private final StockRepository repo;
    private final StockMapper mapper;
    private final DefaultRedisScript<Long> stockLuaScript;
    private final StringRedisTemplate stringRedis;
    /** 扣减并锁定库存（delta 为负） */
    @Override
    @Transactional
    public void lock(StockDeductCmd cmd) {

        for (var i : cmd.getItems()) {

            String key   = KEY_PREFIX + i.getProductId();
            String delta = String.valueOf(-i.getQuantity());   // 负数=扣减

            Long ret = stringRedis.execute(
                    stockLuaScript,
                    Collections.singletonList(key),
                    delta);

            /* ---------- Lua 返回 -2 ⇒ 缓存 miss，回源 DB 并回填 ---------- */
            if (ret != null && ret == LUA_MISS) {                    // ★ 改动
                Stock s = repo.findById(i.getProductId())
                        .orElseThrow(() -> new StockException("库存记录不存在"));
                int available = s.getTotalStock() - s.getLockedStock();

                stringRedis.opsForValue().set(key, String.valueOf(available));

                // 再执行一次 Lua
                ret = stringRedis.execute(
                        stockLuaScript,
                        Collections.singletonList(key),
                        delta);
            }

            /* ---------- 统一检查脚本返回值 ---------- */
            if (ret == null)
                throw new StockException("Redis 执行失败");

            if (ret == -1)
                throw new StockException("库存不足 (productId=" + i.getProductId() + ")");

            // ret ≥ 0 ⇒ 扣减成功，DB 锁定量 +quantity
            repo.addLocked(i.getProductId(), i.getQuantity());
        }
    }


    /* 支付成功：锁定 → 真正扣减 */
    @Transactional
    @Override
    public void confirm(Long orderId, List<StockDeductCmd.Item> items) {
        log.info("[确认库存] order={}", orderId);
    }

    /** 回滚库存（delta 为正） */
    @Override
    @Transactional
    public void rollback(Long orderId, List<StockDeductCmd.Item> items) {
        for (var it : items) {
            String key = "stock:" + it.getProductId();
            String delta = String.valueOf(it.getQuantity());   // 正数 → 回滚

            stringRedis.execute(stockLuaScript,
                    Collections.singletonList(key),
                    delta);

            // 数据库同步 locked_stock -= quantity
            repo.findById(it.getProductId()).ifPresent(s ->
                    s.setLockedStock(s.getLockedStock() - it.getQuantity()));
        }
    }

    @Override
    public StockDTO getByProductId(Long productId) {
        return repo.findById(productId).map(mapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("库存不存在"));
    }

    @Override
    public Stock init(StockInitCmd cmd) {
        return repo.findById(cmd.getProductId())
                .map(s -> {                         // 已存在 → 重置
                    s.setTotalStock(cmd.getTotalStock());
                    s.setLockedStock(0);
                    s.setUpdateTime(LocalDateTime.now());
                    return s;
                })
                .orElseGet(() -> {                 // 不存在 → 新增
                    Stock s = new Stock();
                    s.setProductId(cmd.getProductId());
                    s.setTotalStock(cmd.getTotalStock());
                    s.setLockedStock(0);
                    s.setUpdateTime(LocalDateTime.now());
                    return repo.save(s);
                });
    }

    /* ====== 工具 ====== */
    private String key(Long pid) { return "stock:" + pid; }
}