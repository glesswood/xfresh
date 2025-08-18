package com.xfresh.order.infrastructure.config;
// src/main/java/com/xfresh/order/config/MqConfig.java

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqConfig {

    /** ⽣产订单事件的 Topic 交换机 */
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange("order.exchange");
    }

    // ========== 即时事件 ==========
    @Bean Queue createdStockQ()   { return new Queue("order.created.stock"); }
    @Bean Queue cancelledStockQ() { return new Queue("order.cancelled.stock"); }

    @Bean Binding createdBind(TopicExchange orderExchange) {
        return BindingBuilder.bind(createdStockQ())
                .to(orderExchange).with("order.created");
    }
    @Bean Binding cancelledBind(TopicExchange orderExchange) {
        return BindingBuilder.bind(cancelledStockQ())
                .to(orderExchange).with("order.cancelled");
    }

    // ========== 延迟 TTL ==========
    @Bean Queue ttlQ() {
        return QueueBuilder.durable("order.ttl.queue")
                .ttl(30 * 60 * 1000)           // 30min
                .deadLetterExchange("order.exchange")
                .deadLetterRoutingKey("order.timeout")
                .build();
    }
    @Bean Queue timeoutQ() { return new Queue("order.timeout.stock"); }
    @Bean Binding ttlBind(TopicExchange orderExchange) {
        return BindingBuilder.bind(ttlQ()).to(orderExchange).with("order.ttl");
    }
    @Bean Binding timeoutBind(TopicExchange orderExchange) {
        return BindingBuilder.bind(timeoutQ())
                .to(orderExchange).with("order.timeout");
    }

}