package com.xfresh.event;

public enum OrderEventType {
    ORDER_CREATED,
    ORDER_PAID,
    ORDER_CANCELLED,

    // 新增：库存结果事件（由 stock-service 发出）
    STOCK_DEDUCTED,   // 扣减成功
    STOCK_REJECTED    // 扣减失败/库存不足
}