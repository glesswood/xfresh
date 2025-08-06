package com.xfresh.order.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemDTO {
    private Long productId;
    private BigDecimal price;
    private Integer quantity;
}