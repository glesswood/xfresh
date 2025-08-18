// com/xfresh/order/outbox/OutboxEvent.java
package com.xfresh.order.infrastructure.outbox;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_event",
        indexes = { @Index(name="idx_outbox_status", columnList = "status, id") })
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class OutboxEvent {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String aggregateType;   // "ORDER"
    private String aggregateId;     // 订单ID
    private String eventType;       // ORDER_PAID / ORDER_CANCELLED / ORDER_CREATED
    @Lob
    private String payload;         // JSON

    private String routingKey;      // order.paid / order.cancelled / order.created
    private String exchangeName;    // order.event.exchange

    private Integer status;         // 0=NEW,1=SENT,2=FAILED
    private Integer retryCount;

    private LocalDateTime createdAt;
    private LocalDateTime lastTriedAt;
}