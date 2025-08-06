package com.xfresh.stock.service.Impl;

import com.xfresh.stock.dto.StockDTO;
import com.xfresh.stock.dto.cmd.StockDeductCmd;
import com.xfresh.stock.entity.Stock;
import com.xfresh.stock.mapper.StockMapper;
import com.xfresh.stock.repository.StockRepository;
import com.xfresh.stock.service.StockService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockServiceImpl implements StockService {

    private final StringRedisTemplate redis;
    private final StockRepository repo;
    private final StockMapper mapper;
    private final DefaultRedisScript<Long> script = new DefaultRedisScript<>();

    /* 下单阶段：扣减可用库存 → 增加锁定库存 */
    @Transactional
    @Override
    public void lock(StockDeductCmd cmd) {
        for (StockDeductCmd.Item it : cmd.getItems()) {
            Long left = redis.execute(script,
                    List.of(key(it.getProductId())),
                    String.valueOf(-it.getQuantity()));
            if (left == null || left < 0) {
                // 回滚之前已扣的
                cmd.getItems().forEach(r ->
                        redis.execute(script, List.of(key(r.getProductId())),
                                String.valueOf(r.getQuantity())));
                throw new IllegalStateException("库存不足，商品ID = " + it.getProductId());
            }
        }
        log.info("[预占库存成功] {}", cmd);
        // 视情况：数据库 stock 表也记一笔冻结量
    }

    /* 支付成功：锁定 → 真正扣减 */
    @Transactional
    @Override
    public void confirm(Long orderId, List<StockDeductCmd.Item> items) {
        log.info("[确认库存] order={}", orderId);
    }

    /* 支付失败/取消：释放锁定 */
    @Transactional
    @Override
    public void rollback(Long orderId, List<StockDeductCmd.Item> items) {
        items.forEach(it ->
                redis.execute(script,
                        List.of(key(it.getProductId())),
                        String.valueOf(it.getQuantity())));
        log.info("[回滚库存] order={} items={}", orderId, items);
    }

    @Override
    public StockDTO getByProductId(Long productId) {
        return repo.findById(productId).map(mapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("库存不存在"));
    }
    /* ====== 工具 ====== */
    private String key(Long pid) { return "stock:" + pid; }
}