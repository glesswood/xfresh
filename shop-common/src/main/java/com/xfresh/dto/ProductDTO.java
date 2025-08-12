// product/dto/ProductDTO.java
package com.xfresh.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private Long categoryId;
    private BigDecimal price;
    private Integer stock;
    private Integer status;
    private String description;
}