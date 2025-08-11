package com.xfresh.order.event;
// src/main/java/com/xfresh/order/event/OrderEventPublisher.java

import com.xfresh.order.dto.OrderDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final RabbitTemplate rabbit;

    private static final String EX = "order.exchange";

    public void created(OrderDTO dto) {
        rabbit.convertAndSend(
                "order.exchange",          // 交换机
                "order.created",           // routingKey
                dto                        // ★ 直接丢对象
        );
    }

    public void cancelled(OrderDTO dto) {
        rabbit.convertAndSend(EX, "order.cancelled", dto);
    }

    public void timeout(Long orderId) {   // 供监听器调用
        rabbit.convertAndSend(EX, "order.timeout", orderId);
    }
}
