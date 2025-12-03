package com.fintech.wallet.controller;

import com.fintech.common.exception.InsufficientBalanceException;
import com.fintech.wallet.dto.BalanceOperationRequest;
import com.fintech.wallet.dto.BalanceResponse;
import com.fintech.wallet.dto.WalletCreateRequest;
import com.fintech.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    // Cüzdan oluştur
    // POST http://localhost:8081/api/v1/wallets
    @PostMapping
    private BalanceResponse createWallet(@RequestBody WalletCreateRequest request){
        return walletService.createWallet(request);
    }

    // Para yükle
    @PostMapping("/deposit")
    private BalanceResponse deposit(@RequestBody BalanceOperationRequest request){
        return walletService.deposit(request.userId(), request.currency(), request.amount());
    }

    // Para çek
    @PostMapping("/withdraw")
    private BalanceResponse withdraw(@RequestBody BalanceOperationRequest request) throws InsufficientBalanceException {
        return walletService.withdraw(request.userId(), request.currency(), request.amount());
    }

    // Bakiye sorgula
    @GetMapping("/{userId}/{currency}")
    private BalanceResponse getBalance(@PathVariable Long userId, @PathVariable String currency){
        return walletService.getBalance(userId, currency);
    }

}
