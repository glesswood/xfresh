// product/dto/ProductCreateCmd.java
package com.xfresh.product.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductCreateCmd {
    @NotBlank private String name;
    private Long categoryId;
    @DecimalMin("0.01") private BigDecimal price;
    @Min(0) private Integer stock;
    @Size(max = 2000) private String description;
}