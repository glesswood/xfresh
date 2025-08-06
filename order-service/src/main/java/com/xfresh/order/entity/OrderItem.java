package com.xfresh.order.entity;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "order_item")
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "order_id")
    private Order order;
    @Column(name = "product_id") private Long productId;
    private BigDecimal price;
    private Integer quantity;
    @Column(name = "create_time") private LocalDateTime createTime;
    @Column(name = "update_time") private LocalDateTime updateTime;
}