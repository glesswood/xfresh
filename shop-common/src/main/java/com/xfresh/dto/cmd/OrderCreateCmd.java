package com.xfresh.dto.cmd;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank                   // 前端生成并传入，同一次重试复用同一个
    private String requestId;


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