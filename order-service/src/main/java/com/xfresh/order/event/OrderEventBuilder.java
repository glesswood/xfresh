package com.xfresh.order.event;

import com.xfresh.dto.OrderDTO;
import com.xfresh.event.OrderEvent;
import com.xfresh.event.OrderEventType;
import com.xfresh.order.entity.Order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class OrderEventBuilder {

    public static OrderEvent createdFrom(Order order, List<OrderEvent.Item> items) {
        return baseBuilder(order, items)
                .type(OrderEventType.ORDER_CREATED)
                .build();
    }

    public static OrderEvent paidFrom(Order order, List<OrderEvent.Item> items) {
        return baseBuilder(order, items)
                .type(OrderEventType.ORDER_PAID) // 直接用枚举值
                .build();
    }

    public static OrderEvent cancelledFrom(Order order, List<OrderEvent.Item> items) {
        return baseBuilder(order, items)
                .type(OrderEventType.ORDER_CANCELLED)
                .build();
    }

    private static OrderEvent.OrderEventBuilder baseBuilder(Order order, List<OrderEvent.Item> items) {
        return OrderEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(order.getId())
                .userId(order.getUserId())
                .occurredAt(LocalDateTime.now())
                .items(items);
    }
}