package com.xfresh.dto;

import com.xfresh.dto.cmd.StockDeductCmd;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    public List<StockDeductCmd.Item> toItemList() {
        return this.getItems().stream()
                .map(i -> new StockDeductCmd.Item(i.getProductId(), i.getQuantity()))
                .toList();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemDTO {
        private Long productId;
        private Integer quantity;
        private BigDecimal price;
    }

}