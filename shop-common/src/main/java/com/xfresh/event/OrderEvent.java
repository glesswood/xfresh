package com.xfresh.event;

import com.xfresh.dto.OrderDTO;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEvent implements Serializable {
    private String eventId;          // UUID
    private OrderEventType type;
    private Long orderId;
    private Long userId;
    private BigDecimal totalAmount;
    private LocalDateTime occurredAt;

    @Singular
    private List<Item> items;

    // 新增：可选的失败原因（仅在 STOCK_REJECTED 时使用）
    private String reason;


    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Item implements Serializable {
        private Long productId;
        private Integer quantity;
        private BigDecimal price; // 可选，用于对账
    }
}