package com.fintech.wallet.dto;

import java.math.BigDecimal;
import java.util.List;

public record PortfolioResponse(
        BigDecimal balance,           // Nakit
        BigDecimal totalAssetValue,   // Hisselerin Toplamı
        BigDecimal totalWealth,       // Nakit + Hisseler
        List<AssetResponse> assets         // Detay liste
) {}