package com.xfresh.stock.service.Impl;

import com.xfresh.event.OrderEvent;
import com.xfresh.stock.exception.StockException;
import com.xfresh.stock.repository.StockRepository;
import com.xfresh.stock.service.OrderEventApplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 把订单事件“应用”到库存：锁定/确认扣减/回滚
 * 这里直接调用你的 StockRepository 的原子更新方法(Lua/SQL)即可
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventApplyServiceImpl implements OrderEventApplyService {

    private final StockRepository repo;

    @Transactional
    @Override
    public void onOrderCreated(OrderEvent ev) {
        // 锁定库存：对 ev.items 遍历调用 repo.lock(...) 或 Lua 脚本
        ev.getItems().forEach(it -> {
            int changed = repo.addLocked(it.getProductId(), it.getQuantity(),LocalDateTime.now());
            if (changed == 0) {
                throw new StockException("库存不足 (productId=" + it.getProductId() + ")");
            }
        });
    }

    @Transactional
    @Override
    public void onOrderPaid(OrderEvent ev) {
        // 真正扣减：totalStock-=qty, lockedStock-=qty（你的 confirmDeduct 方法）
        ev.getItems().forEach(it -> {
            int changed = repo.confirmDeduct(it.getProductId(), it.getQuantity(), LocalDateTime.now());
            if (changed == 0) {
                throw new StockException("确认扣减失败 (productId=" + it.getProductId() + ")");
            }
        });
    }

    @Transactional
    @Override
    public void onOrderCancelled(OrderEvent ev) {
        // 回滚锁定：lockedStock-=qty（归还至可用库存）
        ev.getItems().forEach(it -> {
            int changed = repo.rollbackLock(it.getProductId(), it.getQuantity(),LocalDateTime.now());
            if (changed == 0) {
                throw new StockException("回滚失败 (productId=" + it.getProductId() + ")");
            }
        });
    }
}