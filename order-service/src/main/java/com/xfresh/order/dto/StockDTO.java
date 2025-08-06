package com.xfresh.order.dto;

import lombok.Data;

@Data
public class StockDTO {
    private Long productId;
    private Integer totalStock;
    private Integer lockedStock;
}