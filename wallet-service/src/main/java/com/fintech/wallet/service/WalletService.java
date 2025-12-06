package com.fintech.wallet.service;

import com.fintech.common.exception.InsufficientBalanceException;
import com.fintech.wallet.dto.BalanceResponse;
import com.fintech.wallet.dto.PortfolioResponse;
import com.fintech.wallet.dto.WalletCreateRequest;
import com.fintech.wallet.model.Asset;

import java.math.BigDecimal;
import java.util.List;

public interface WalletService {

    // Yeni cüzdan oluştur
    BalanceResponse createWallet(WalletCreateRequest request);
    // Bakiye getir
    BalanceResponse getBalance(Long userId, String currency);
    // Para yükle
    BalanceResponse deposit(Long userId, String currency, BigDecimal amount);
    // Para çek
    BalanceResponse withdraw(Long userId, String currency, BigDecimal amount) throws InsufficientBalanceException;

    // ESKİ withdraw/lockAsset YERİNE BUNLARI KULLANACAĞIZ:
    void executeBuyOrder(Long userId, String symbol, BigDecimal amount, BigDecimal price) throws InsufficientBalanceException;
    void executeSellOrder(Long userId, String symbol, BigDecimal amount, BigDecimal price) throws InsufficientBalanceException;
    List<Asset> getUserAssets(Long userId);
    PortfolioResponse getUserPortfolio(Long userId);
}
