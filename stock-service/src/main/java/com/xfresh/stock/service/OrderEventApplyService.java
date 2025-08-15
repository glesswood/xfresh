package com.xfresh.stock.service;

import com.xfresh.event.OrderEvent;

public interface OrderEventApplyService {
    void onOrderCreated(OrderEvent ev);
    void onOrderPaid(OrderEvent ev);
    void onOrderCancelled(OrderEvent ev);
}