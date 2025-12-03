package com.fintech.common.exception;

public class InsufficientBalanceException extends Exception { // Dikkat: RuntimeException DEĞİL
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
