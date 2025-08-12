package com.xfresh.order.event;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static com.xfresh.order.config.RabbitPayTimeoutConfig.*;

@Component
@RequiredArgsConstructor
public class PayTimeoutSender {

    private final RabbitTemplate rabbitTemplate;

    // 5 分钟（毫秒）
    private static final long TTL_MS = 5 * 60 * 1000L;

    public void send(Long orderId) {
        MessagePostProcessor mpp = msg -> {
            msg.getMessageProperties().setExpiration(String.valueOf(TTL_MS)); // per-message TTL
            return msg;
        };
        // 仅发送 Long（可序列化）
        rabbitTemplate.convertAndSend(EXCHANGE, DELAY_ROUTING_KEY, orderId, mpp);
    }
}