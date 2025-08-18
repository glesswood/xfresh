package com.xfresh.stock.application.service.Impl;

import com.xfresh.event.OrderEvent;
import com.xfresh.stock.application.service.OrderEventApplyService;
import com.xfresh.stock.domain.StockDomainHandler;
import com.xfresh.stock.domain.repository.StockRepository;
import com.xfresh.stock.infrastructure.cache.StockCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderEventApplyServiceImpl implements OrderEventApplyService {

    private final StockDomainHandler domain;
    private final StockRepository stockRepo;
    private final StockCacheService cache;

    @Override
    @Transactional
    public void onOrderCreated(OrderEvent ev) {
        // 1) 预热
        ev.getItems().forEach(it ->
                stockRepo.findById(it.getProductId()).ifPresent(s ->
                        cache.warmIfAbsent(s.getProductId(), s.getTotalStock() - s.getLockedStock())
                )
        );
        // 2) Redis 预扣（快速失败）
        for (var it : ev.getItems()) {
            boolean ok = cache.tryReserve(it.getProductId(), it.getQuantity());
            if (!ok) throw new RuntimeException("库存不足(redis) pid=" + it.getProductId());
        }
        // 3) DB 条件加锁为准；失败则补偿 Redis
        try {
            var list = ev.getItems().stream()
                    .map(i -> new StockDomainHandler.SkuQty(i.getProductId(), i.getQuantity()))
                    .toList();
            domain.lock(list);
        } catch (RuntimeException e) {
            ev.getItems().forEach(it -> cache.release(it.getProductId(), it.getQuantity()));
            throw e;
        }
    }

    @Override
    @Transactional
    public void onOrderPaid(OrderEvent ev) {
        var list = ev.getItems().stream()
                .map(i -> new StockDomainHandler.SkuQty(i.getProductId(), i.getQuantity()))
                .toList();
        domain.confirm(list);
        // 可选异步校正：根据 DB 可用，覆盖 Redis
        // ev.getItems().forEach(it ->
        //     stockRepo.findById(it.getProductId()).ifPresent(s ->
        //         cache.setAvail(s.getProductId(), s.getTotalStock() - s.getLockedStock())
        //     ));
    }

    @Override
    @Transactional
    public void onOrderCancelled(OrderEvent ev) {
        var list = ev.getItems().stream()
                .map(i -> new StockDomainHandler.SkuQty(i.getProductId(), i.getQuantity()))
                .toList();
        domain.rollback(list);
        // DB 成功后补偿 Redis
        ev.getItems().forEach(it -> cache.release(it.getProductId(), it.getQuantity()));
    }
}