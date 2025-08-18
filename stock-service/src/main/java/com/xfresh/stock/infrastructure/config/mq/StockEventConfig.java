package com.xfresh.stock.infrastructure.config.mq;


import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StockEventConfig {
    public static final String EXCHANGE = "stock.event.exchange";
    public static final String RK_DEDUCTED = "stock.deducted";
    public static final String RK_REJECTED = "stock.rejected";

    @Bean
    public TopicExchange stockEventExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }
}