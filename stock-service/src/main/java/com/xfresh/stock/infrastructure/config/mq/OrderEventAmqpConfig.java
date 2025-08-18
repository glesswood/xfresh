package com.xfresh.stock.infrastructure.config.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// stock-service/src/main/java/.../mq/OrderEventAmqpConfig.java
@Configuration
public class OrderEventAmqpConfig {

    public static final String ORDER_EVENT_EXCHANGE = "order.event.exchange";
    public static final String STOCK_QUEUE = "order.event.stock.q";

    @Bean
    public TopicExchange orderEventExchange() {
        return ExchangeBuilder.topicExchange(ORDER_EVENT_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue stockQueue() {
        return QueueBuilder.durable(STOCK_QUEUE)
                // 可选：失败消息进 DLX
                .withArgument("x-dead-letter-exchange", "order.event.dlx")
                .withArgument("x-dead-letter-routing-key", "order.event.dlx")
                .build();
    }

    @Bean
    public Binding bindCreated() {
        // 如果只想收 created：routingKey 用 "order.created"
        return BindingBuilder.bind(stockQueue())
                .to(orderEventExchange()).with("order.*");
    }

    @Bean
    public MessageConverter jacksonMessageConverter(ObjectMapper om) {
        om.findAndRegisterModules();
        return new Jackson2JsonMessageConverter(om);
    }
}