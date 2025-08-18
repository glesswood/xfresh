// com/xfresh/order/outbox/OutboxPublisher.java
package com.xfresh.order.infrastructure.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xfresh.event.OrderEvent;
import com.xfresh.mq.RabbitNames;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxRepository repo;
    private final ObjectMapper om;

    /**
     * 新增：直接接收领域事件 OrderEvent 的入口
     * 让 serviceImpl 里的 outboxPublisher.append(event) 能编译通过
     */
    public void append(OrderEvent ev) {
        try {
            // 1) 序列化 payload（直接序列化 OrderEvent）
            String payload = om.writeValueAsString(ev);

            // 2) 根据事件类型映射 routing key
            String routingKey = switch (ev.getType()) {
                case ORDER_CREATED   -> "order.created";
                case ORDER_PAID      -> "order.paid";
                case ORDER_CANCELLED -> "order.cancelled";
                default -> throw new IllegalArgumentException("未知事件类型: " + ev.getType().name());
            };

            // 3) 落 Outbox 表
            OutboxEvent out = OutboxEvent.builder()
                    .aggregateType("ORDER")
                    .aggregateId(String.valueOf(ev.getOrderId()))
                    .eventType(ev.getType().name())
                    .payload(payload)
                    .routingKey(routingKey)
                    .exchangeName(RabbitNames.ORDER_EVENT_EXCHANGE)
                    .status(0)                 // 0=NEW
                    .retryCount(0)
                    .createdAt(LocalDateTime.now())
                    .build();

            repo.save(out);
        } catch (Exception e) {
            throw new RuntimeException("append outbox failed", e);
        }
    }


}