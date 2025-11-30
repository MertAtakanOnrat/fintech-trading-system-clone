package com.fintech.wallet.service.impl;

import com.fintech.wallet.dto.BalanceResponse;
import com.fintech.wallet.dto.WalletCreateRequest;
import com.fintech.wallet.model.Wallet;
import com.fintech.wallet.repository.WalletRepository;
import com.fintech.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;

    @Override
    public BalanceResponse createWallet(WalletCreateRequest request) {
        if (walletRepository.existsByUserIdAndCurrency(request.userId(), request.currency())){
            throw new RuntimeException("Wallet already exists for user " + request.userId());
        }

        // Builder ile nesne oluşturma (Builder Pattern)
        Wallet newWallet = Wallet.builder()
                .userId(request.userId())
                .currency(request.currency())
                .balance(BigDecimal.ZERO)
                .build();

        Wallet savedWallet = walletRepository.save(newWallet);

        return mapToResponse(savedWallet);
    }

    @Override
    @Transactional(readOnly = true) // Sadece okuma yapıldığı için performans artırır
    public BalanceResponse getBalance(Long userId, String currency) {
        Wallet wallet = getWalletOrThrow(userId, currency);
        return mapToResponse(wallet);
    }

    @Override
    public BalanceResponse deposit(Long userId, String currency, BigDecimal amount) {
        Wallet wallet = getWalletOrThrow(userId, currency);

        // Bakiye ekleme (BigDecimal immutable'dır, atama yapmak gerekir)
        wallet.setBalance(wallet.getBalance().add(amount));

        Wallet savedWallet = walletRepository.save(wallet);
        return mapToResponse(savedWallet);
    }

    @Override
    public BalanceResponse withdraw(Long userId, String currency, BigDecimal amount) {
        Wallet wallet = getWalletOrThrow(userId, currency);

        // Yetersiz bakiye kontrolü
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // Para çekme
        wallet.setBalance(wallet.getBalance().subtract(amount));

        Wallet savedWallet = walletRepository.save(wallet);
        return mapToResponse(savedWallet);
    }

    // Yardımcı Metot: Cüzdan bulamazsa hata fırlatır
    private Wallet getWalletOrThrow(Long userId, String currency){
        return walletRepository.findByUserIdAndCurrency(userId, currency)
                .orElseThrow(() -> new RuntimeException("Wallet not found for user " + userId));
    }

    // Yardımcı Metot: Entity -> DTO dönüşümü
    private BalanceResponse mapToResponse(Wallet wallet){
        return new BalanceResponse(wallet.getBalance(), wallet.getCurrency());
    }
}
