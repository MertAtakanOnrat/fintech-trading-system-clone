package com.fintech.wallet.dto;

import java.math.BigDecimal;

public record BalanceOperationRequest(
        Long userId,
        String currency,
        BigDecimal amount
) {}
