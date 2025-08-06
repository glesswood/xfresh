package com.xfresh.order.entity;
import com.xfresh.order.dto.cmd.StockDeductCmd;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "`order`")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "order_no")  private String orderNo;
    @Column(name = "user_id")   private Long userId;
    @Column(name = "total_amount") private BigDecimal totalAmount;
    @Column(columnDefinition = "TINYINT DEFAULT 1")
    private Integer status;           // 0/1/2
    @Column(name = "create_time") private LocalDateTime createTime;
    @Column(name = "update_time") private LocalDateTime updateTime;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items = new ArrayList<>();

    public List<StockDeductCmd.Item> itemsToStockCmd() {
        return items.stream()
                .map(it -> new StockDeductCmd.Item(it.getProductId(), it.getQuantity()))
                .toList();
    }
}