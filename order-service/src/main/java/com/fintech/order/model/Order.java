package com.fintech.order.model;

import com.fintech.order.domain.enums.OrderSide;
import com.fintech.order.domain.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders") // DİKKAT: SQL keyword çakışmasını önlemek için 'orders'
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String symbol; // Örn: TYHAO, APPL

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderSide side; // BUY - SELL

    @Column(nullable = false,precision = 19, scale = 2)
    private BigDecimal price; // Emir Fiyatı

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected  void onCreate(){
        createdAt = LocalDateTime.now();
        if (status == null){
            status = OrderStatus.PENDING; // Saga'nın 1. Adımı: Her emir PENDING başlar
        }
    }
}
