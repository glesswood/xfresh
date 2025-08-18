package com.xfresh.order.domain.entity;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

    private Long productId;
    private Integer quantity;
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @ToString.Exclude       // 防止循环 toString
    private Order order;

    @CreationTimestamp
    private LocalDateTime createTime;
    @UpdateTimestamp
    private LocalDateTime updateTime;
}