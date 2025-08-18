package com.xfresh.order.domain.constant;
/** 订单状态：0-已取消 1-待支付 2-已支付 … 后面需要再补充 */
public interface OrderStatus {
    int CANCELLED = 0;
    int PENDING   = 1;
    int PAID      = 2;
}
