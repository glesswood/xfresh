package com.xfresh.order.event;

import com.xfresh.order.config.RabbitPayTimeoutConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static com.xfresh.order.config.RabbitPayTimeoutConfig.*;

// PayTimeoutSender.java
@Component
@RequiredArgsConstructor
public class PayTimeoutSender {
    private final RabbitTemplate rabbitTemplate;
    private static final long TTL_MS = 60 * 1000L;

    public void send(Long orderId) {
        MessagePostProcessor mpp = msg -> {
            msg.getMessageProperties().setExpiration(String.valueOf(TTL_MS));
            msg.getMessageProperties().setContentType("application/json"); // 保险起见
            return msg;
        };
        rabbitTemplate.convertAndSend(
                RabbitPayTimeoutConfig.EXCHANGE,
                RabbitPayTimeoutConfig.DELAY_ROUTING_KEY,
                java.util.Map.of("orderId", orderId),  // ★ 改成 Map
                mpp
        );
    }
}