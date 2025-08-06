package com.xfresh.order.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    private Integer status;              // 0-待支付 1-已支付…
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private List<OrderItemDTO> items;    // 子表
}