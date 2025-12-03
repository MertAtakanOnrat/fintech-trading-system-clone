package com.fintech.wallet.service;

import com.fintech.common.exception.InsufficientBalanceException;
import com.fintech.wallet.dto.BalanceResponse;
import com.fintech.wallet.dto.WalletCreateRequest;

import java.math.BigDecimal;

public interface WalletService {
    // Yeni cüzdan oluştur
    BalanceResponse createWallet(WalletCreateRequest request);

    // Bakiye getir
    BalanceResponse getBalance(Long userId, String currency);

    // Para yükle
    BalanceResponse deposit(Long userId, String currency, BigDecimal amount);

    // Para çek
    BalanceResponse withdraw(Long userId, String currency, BigDecimal amount) throws InsufficientBalanceException;


}
