package com.fintech.wallet.dto;

import java.math.BigDecimal;

public record BalanceResponse(
        BigDecimal balance,
        String currency
) { }
