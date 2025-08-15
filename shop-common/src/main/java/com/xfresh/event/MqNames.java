package com.xfresh.event;

public interface MqNames {
    String ORDER_EVENT_EXCHANGE = "order.event.exchange";
    String ORDER_EVENT_STOCK_Q  = "order.event.stock.q"; // 若 stock-service 要消费
    String ORDER_EVENT_ROUTING_ALL = "order.*";
}