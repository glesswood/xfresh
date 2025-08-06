package com.xfresh.stock.dto.cmd;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class StockDeductCmd {          // 批量扣减
    @NotEmpty
    private List<Item> items;

    @Data public static class Item {
        private Long productId;
        private Integer quantity;
    }
}