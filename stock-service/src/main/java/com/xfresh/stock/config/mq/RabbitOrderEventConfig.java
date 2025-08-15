/*
package com.xfresh.stock.config.mq;


import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitOrderEventConfig {

    // 与 order-service 保持一致
    public static final String ORDER_EVENT_EXCHANGE = "order.event.exchange";

    // 本服务消费的队列（你可以改名）
    public static final String ORDER_EVENT_STOCK_QUEUE = "order.event.stock.q";

    // 路由键（与 OutboxPublisher 里保持一致）
    public static final String RK_CREATED   = "order.created";
    public static final String RK_PAID      = "order.paid";
    public static final String RK_CANCELLED = "order.cancelled";

    @Bean
    public TopicExchange orderEventExchange() {
        return new TopicExchange(ORDER_EVENT_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderEventStockQueue() {
        // 持久化队列
        return QueueBuilder.durable(ORDER_EVENT_STOCK_QUEUE).build();
    }

    @Bean
    public Binding bindCreated() {
        return BindingBuilder.bind(orderEventStockQueue())
                .to(orderEventExchange()).with(RK_CREATED);
    }

    @Bean
    public Binding bindPaid() {
        return BindingBuilder.bind(orderEventStockQueue())
                .to(orderEventExchange()).with(RK_PAID);
    }

    @Bean
    public Binding bindCancelled() {
        return BindingBuilder.bind(orderEventStockQueue())
                .to(orderEventExchange()).with(RK_CANCELLED);
    }
}*/
