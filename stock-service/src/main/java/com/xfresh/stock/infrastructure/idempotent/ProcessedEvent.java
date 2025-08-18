package com.xfresh.stock.infrastructure.idempotent;

import jakarta.persistence.*;


import lombok.*;

@Entity
@Table(name = "processed_event", uniqueConstraints = {
        @UniqueConstraint(name = "uk_event", columnNames = "event_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProcessedEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "event_id", nullable = false, length = 64)
    private String eventId;
    @Column(name = "event_type", nullable = false, length = 32)
    private String eventType;
    @Column(name = "aggregate_id", nullable = false)
    private Long aggregateId;
}