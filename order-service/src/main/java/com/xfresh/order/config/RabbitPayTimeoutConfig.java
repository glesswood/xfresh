package com.xfresh.order.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class RabbitPayTimeoutConfig {

    // 业务交换机（发延时消息用）
    public static final String EXCHANGE = "order.pay.exchange";
    // 死信交换机（TTL 到期后的投递处）
    public static final String DLX_EXCHANGE = "order.pay.dlx";

    // 延时队列（不被消费，只用于 TTL）
    public static final String DELAY_QUEUE = "order.pay.delay.q";
    public static final String DELAY_ROUTING_KEY = "order.pay.delay";

    // 超时处理队列（真正被消费）
    public static final String TIMEOUT_QUEUE = "order.pay.timeout.q";
    public static final String TIMEOUT_ROUTING_KEY = "order.pay.timeout";

    @Bean
    public DirectExchange orderPayExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange orderPayDlx() {
        return new DirectExchange(DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderPayDelayQueue() {
        return QueueBuilder.durable(DELAY_QUEUE)
                .withArguments(Map.of(
                        "x-dead-letter-exchange", DLX_EXCHANGE,
                        "x-dead-letter-routing-key", TIMEOUT_ROUTING_KEY
                ))
                .build();
    }

    @Bean
    public Queue orderPayTimeoutQueue() {
        return QueueBuilder.durable(TIMEOUT_QUEUE).build();
    }

    @Bean
    public Binding bindDelay() {
        return BindingBuilder.bind(orderPayDelayQueue())
                .to(orderPayExchange())
                .with(DELAY_ROUTING_KEY);
    }

    @Bean
    public Binding bindTimeout() {
        return BindingBuilder.bind(orderPayTimeoutQueue())
                .to(orderPayDlx())
                .with(TIMEOUT_ROUTING_KEY);
    }
}