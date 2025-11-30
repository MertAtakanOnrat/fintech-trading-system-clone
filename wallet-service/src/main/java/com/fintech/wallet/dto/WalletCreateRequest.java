package com.fintech.wallet.dto;

public record WalletCreateRequest(
        Long userId,
        String currency
){}
