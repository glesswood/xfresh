// product/dto/ProductUpdateCmd.java
package com.xfresh.product.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductUpdateCmd {
    @NotNull private Long id;
    private String name;
    private Long categoryId;
    @DecimalMin("0.01") private BigDecimal price;
    @Min(0) private Integer stock;
    private Integer status;
    private String description;
}