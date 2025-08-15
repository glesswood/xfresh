package com.xfresh.event.dto;

// com/xfresh/order/event/OrderEventDto.java

import com.xfresh.event.OrderEventType;
import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderEventDto implements Serializable {
    private Long orderId;
    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    private Integer status;             // 1=待支付,2=已支付,0=已取消
    private LocalDateTime occurredAt;

    private OrderEventType type;
    private List<Item> items;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Item implements Serializable {
        private Long productId;
        private Integer quantity;
    }
}
