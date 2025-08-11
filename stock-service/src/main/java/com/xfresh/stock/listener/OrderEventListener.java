package com.xfresh.stock.listener;

// src/main/java/com/xfresh/stock/listener/OrderEventListener.java

import com.xfresh.order.dto.OrderDTO;
import com.xfresh.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final StockService stockService;

    /** 订单取消或超时 → 归还库存 */
    @RabbitListener(queues = { "order.cancelled.stock", "order.timeout.stock" })
    public void onRollback(Object payload) {
        if (payload instanceof OrderDTO dto) {
            stockService.rollback(dto.getId(), dto.toItemList());
            log.info("[库存回滚] orderId={}", dto.getId());
        } else if (payload instanceof Long orderId) { // timeout 队列只放 orderId
            log.info("[库存回滚-超时] orderId={}", orderId);
        }
    }
}