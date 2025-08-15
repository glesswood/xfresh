package com.xfresh.mq;


public interface RabbitNames {
    String ORDER_EVENT_EXCHANGE = "order.event.exchange";
    // 还可以统一放 routingKey 常量：order.created / order.paid / order.cancelled
}