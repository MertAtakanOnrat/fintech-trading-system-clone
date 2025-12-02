package com.fintech.common.event;

public record WalletEvent(
        String orderId,
        String status // "SUCCESS" veya "FAILED"
) {}
