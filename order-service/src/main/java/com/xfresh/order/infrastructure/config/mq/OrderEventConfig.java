package com.xfresh.order.infrastructure.config.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
// order-service/src/main/java/.../mq/OrderEventConfig.java
@Configuration
public class OrderEventConfig {

    public static final String ORDER_EVENT_EXCHANGE = "order.event.exchange";

    @Bean
    public TopicExchange orderEventExchange() {
        return ExchangeBuilder.topicExchange(ORDER_EVENT_EXCHANGE)
                .durable(true)
                .build();
    }

    /** 建议统一用 Jackson 的消息转换器发 JSON（生产端/消费端都配） */
    @Bean
    public MessageConverter jacksonMessageConverter(ObjectMapper objectMapper) {
        // 支持 LocalDateTime 等
        objectMapper.findAndRegisterModules();
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}