// domain/StockDomainHandler.java
package com.xfresh.stock.domain;

import com.xfresh.stock.domain.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockDomainHandler {

    private final StockRepository stockRepository;

    /**
     * Try：锁定库存（用于 ORDER_CREATED）
     * 同一事务中，任一 SKU 锁失败则抛异常 -> 回滚整个订单的所有锁操作
     */
    @Transactional
    public void lock(List<SkuQty> items) {
        // 固定顺序减少死锁概率：对 skuId 排序后逐一更新
        items.stream()
                .sorted(Comparator.comparing(SkuQty::skuId))
                .forEach(i -> {
                    int n = stockRepository.addLocked(i.skuId(), i.qty(),LocalDateTime.now());
                    if (n <= 0) {
                        throw new InsufficientStockException("TryLock fail, sku=" + i.skuId());
                    }
                });
        log.debug("lock ok, items={}", items);
    }

    /**
     * Confirm：确认扣减（用于 ORDER_PAID）
     */
    @Transactional
    public void confirm(List<SkuQty> items) {
        items.stream()
                .sorted(Comparator.comparing(SkuQty::skuId))
                .forEach(i -> {
                    int n = stockRepository.confirmDeduct(i.skuId(), i.qty(), LocalDateTime.now());
                    if (n <= 0) {
                        throw new IllegalStateException("Confirm fail (locked < qty?), sku=" + i.skuId());
                    }
                });
        log.debug("confirm ok, items={}", items);
    }

    /**
     * Rollback：回滚锁定（用于 ORDER_CANCELLED 或支付失败）
     */
    @Transactional
    public void rollback(List<SkuQty> items) {
        items.stream()
                .sorted(Comparator.comparing(SkuQty::skuId))
                .forEach(i -> {
                    int n = stockRepository.rollbackLock(i.skuId(), i.qty(), LocalDateTime.now());
                    if (n <= 0) {
                        // 并不是致命错误：可能之前根本没锁成功
                        log.warn("rollback no-op (locked not enough?), sku={}", i.skuId());
                    }
                });
        log.debug("rollback ok, items={}", items);
    }

    // === 简单的值对象 ===
    public record SkuQty(Long skuId, Integer qty) {}

    // === 业务异常 ===
    public static class InsufficientStockException extends RuntimeException {
        public InsufficientStockException(String msg) { super(msg); }
    }
}