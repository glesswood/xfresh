package com.xfresh.stock.application.service;

import com.xfresh.event.OrderEvent;

public interface OrderEventApplyService {
    void onOrderCreated(OrderEvent ev);
    void onOrderPaid(OrderEvent ev);
    void onOrderCancelled(OrderEvent ev);
}