package com.fintech.matching.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LimitOrder implements Comparable<LimitOrder> {
    private String orderId;
    private Long userId;
    private String symbol;
    private BigDecimal amount; // Bu değer eşleşme oldukça azalacak (Mutable)
    private BigDecimal price;
    private String side; // BUY - SELL
    private long timestamp;

    @Override
    public int compareTo(LimitOrder other) {
        // 1. Fiyat Önceliği
        if ("BUY".equals(this.side)) {
            // Alıcılar: Fiyatı Yüksek olan öne geçer
            int priceComp = other.price.compareTo(this.price);
            if (priceComp != 0) return priceComp;
        } else {
            // Satıcılar: Fiyatı Düşük olan öne geçer
            int priceComp = this.price.compareTo(other.price);
            if (priceComp != 0) return priceComp;
        }

        // 2. Zaman Önceliği (FIFO)
        return Long.compare(this.timestamp, other.timestamp);
    }
}