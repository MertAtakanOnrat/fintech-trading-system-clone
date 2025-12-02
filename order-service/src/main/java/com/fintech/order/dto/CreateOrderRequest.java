package com.fintech.order.dto;

import com.fintech.order.domain.enums.OrderSide;

import java.math.BigDecimal;

public record CreateOrderRequest(
        Long userId,
        String symbol,       // Hangi hisse?
        OrderSide side,      // Al mı Sat mı?
        BigDecimal amount,   // Kaç adet?
        BigDecimal price     // Hangi fiyattan?
) {}
