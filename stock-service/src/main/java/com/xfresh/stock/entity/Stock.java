package com.xfresh.stock.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

// com.xfresh.stock.entity.Stock
@Data
@Entity
@Table(name = "stock")
public class Stock {

    @Id
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "total_stock")
    private Integer totalStock;

    @Column(name = "locked_stock")
    private Integer lockedStock;

    @Column(name = "update_time")
    private LocalDateTime updateTime;
}