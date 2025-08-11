package com.xfresh.order.dto.cmd;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class StockInitCmd {

    @NotNull
    private Long productId;

    @Min(0)
    private Integer totalStock;
}
