package com.fintech.common.event;

import java.math.BigDecimal;

public record TradeExecutedEvent(
        String symbol,
        String buyOrderId,
        String sellOrderId,
        Long buyerId,   // YENİ: Alan kişi kim?
        Long sellerId,  // YENİ: Satan kişi kim?
        BigDecimal price,
        BigDecimal amount,
        long timestamp
) {}