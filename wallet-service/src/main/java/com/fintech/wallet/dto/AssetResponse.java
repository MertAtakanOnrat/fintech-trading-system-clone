package com.fintech.wallet.dto;

import java.math.BigDecimal;

public record AssetResponse(
        String symbol,
        BigDecimal amount,
        BigDecimal currentPrice, // O anki piyasa değeri (Opsiyonel, şimdilik null geçebiliriz)
        BigDecimal totalValue
) {}