package com.xfresh.stock.service;
// stock-service/src/main/java/.../service/StockEventPublisher.java

import com.xfresh.event.OrderEvent;
import com.xfresh.event.OrderEventType;
import com.xfresh.stock.config.mq.StockEventConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class StockEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public void publishStockDeducted(OrderEvent paidEvent) {
        OrderEvent out = OrderEvent.builder()
                .eventId(paidEvent.getEventId())
                .type(OrderEventType.STOCK_DEDUCTED)
                .orderId(paidEvent.getOrderId())
                .userId(paidEvent.getUserId())
                .totalAmount(paidEvent.getTotalAmount())
                .occurredAt(LocalDateTime.now())
                .items(paidEvent.getItems())      // 直接复用原 items
                .build();

        rabbitTemplate.convertAndSend(
                StockEventConfig.EXCHANGE, StockEventConfig.RK_DEDUCTED, out);
    }

    public void publishStockRejected(OrderEvent paidEvent, String reason) {
        OrderEvent out = OrderEvent.builder()
                .eventId(paidEvent.getEventId())
                .type(OrderEventType.STOCK_REJECTED)
                .orderId(paidEvent.getOrderId())
                .userId(paidEvent.getUserId())
                .totalAmount(paidEvent.getTotalAmount())
                .occurredAt(LocalDateTime.now())
                .items(paidEvent.getItems())
                .reason(reason)                   // 只有拒绝时才填
                .build();

        rabbitTemplate.convertAndSend(
                StockEventConfig.EXCHANGE, StockEventConfig.RK_REJECTED, out);
    }
}