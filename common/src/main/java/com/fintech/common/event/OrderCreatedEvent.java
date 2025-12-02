package com.fintech.common.event;

import java.math.BigDecimal;

// Java Record (Data Taşıyıcı)
public record OrderCreatedEvent(
        String orderId,
        Long userId,
        String symbol,       // Hangi hisse?
        BigDecimal amount,   // Kaç adet?
        BigDecimal price,    // Fiyatı ne?
        String side          // BUY / SELL (Enum yerine String taşıyoruz, daha güvenli)
) {
}
