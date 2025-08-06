package com.xfresh.order.dto.cmd;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreateCmd {

    @NotNull
    private Long userId;

    /**
     * 订单里包含的商品明细
     */
    @NotEmpty
    private List<ItemCmd> items;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ItemCmd {
        @NotNull
        private Long productId;

        @NotNull
        @Min(1)
        private Integer quantity;
    }
}