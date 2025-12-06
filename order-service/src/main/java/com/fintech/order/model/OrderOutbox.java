package com.fintech.order.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_outbox")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    private String eventType;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private LocalDateTime createdAt;

    // --- YENİ EKLENEN ---
    @Column(nullable = false)
    private Boolean processed = false;
    // --------------------

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (processed == null) processed = false;
    }
}