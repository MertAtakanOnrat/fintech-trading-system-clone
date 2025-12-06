package com.fintech.wallet.service.impl;

import com.fintech.common.exception.InsufficientBalanceException;
import com.fintech.wallet.dto.AssetResponse;
import com.fintech.wallet.dto.BalanceResponse;
import com.fintech.wallet.dto.PortfolioResponse;
import com.fintech.wallet.dto.WalletCreateRequest;
import com.fintech.wallet.model.Asset;
import com.fintech.wallet.model.Wallet;
import com.fintech.wallet.repository.AssetRepository;
import com.fintech.wallet.repository.WalletRepository;
import com.fintech.wallet.service.WalletMarketService;
import com.fintech.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final AssetRepository assetRepository; // Constructor'a (RequiredArgsConstructor) otomatik eklenir
    private final WalletMarketService walletMarketService; // Inject etmeyi unutma (@RequiredArgsConstructor yapar)

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
    public BalanceResponse withdraw(Long userId, String currency, BigDecimal amount) throws InsufficientBalanceException {
        Wallet wallet = getWalletOrThrow(userId, currency);

        if (wallet.getBalance().compareTo(amount) < 0) {
            // BURASI DEĞİŞTİ: Artık rollback'i tetiklemeyen bir hata fırlatıyoruz
            throw new InsufficientBalanceException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));

        Wallet savedWallet = walletRepository.save(wallet);
        return mapToResponse(savedWallet);
    }

    @Override
    @Transactional
    public void executeBuyOrder(Long userId, String symbol, BigDecimal amount, BigDecimal price) throws InsufficientBalanceException {
        // 1. Cüzdanı Bul
        Wallet wallet = getWalletOrThrow(userId, "TRY");

        // 2. Tutar Hesapla
        BigDecimal totalCost = price.multiply(amount);

        // 3. Para Kontrolü ve Düşüşü
        if (wallet.getBalance().compareTo(totalCost) < 0) {
            throw new InsufficientBalanceException("Insufficient funds for BUY order");
        }
        wallet.setBalance(wallet.getBalance().subtract(totalCost));
        walletRepository.save(wallet);

        // 4. HİSSE EKLEME (İşte eksik olan parça!)
        Asset asset = assetRepository.findByUserIdAndSymbol(userId, symbol)
                .orElse(Asset.builder()
                        .userId(userId)
                        .symbol(symbol)
                        .availableAmount(BigDecimal.ZERO)
                        .lockedAmount(BigDecimal.ZERO)
                        .build());

        asset.setAvailableAmount(asset.getAvailableAmount().add(amount));
        assetRepository.save(asset);
    }

    @Override
    @Transactional
    public void executeSellOrder(Long userId, String symbol, BigDecimal amount, BigDecimal price) throws InsufficientBalanceException {
        // 1. Hisse Kontrolü
        Asset asset = assetRepository.findByUserIdAndSymbol(userId, symbol)
                .orElseThrow(() -> new InsufficientBalanceException("Asset not found for SELL order"));

        if (asset.getAvailableAmount().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient asset quantity");
        }

        // 2. Hisse Düşüşü
        asset.setAvailableAmount(asset.getAvailableAmount().subtract(amount));
        assetRepository.save(asset);

        // 3. PARA EKLEME (İşte eksik olan parça!)
        Wallet wallet = getWalletOrThrow(userId, "TRY");
        BigDecimal totalGain = price.multiply(amount);

        wallet.setBalance(wallet.getBalance().add(totalGain));
        walletRepository.save(wallet);
    }

    @Override
    public PortfolioResponse getUserPortfolio(Long userId) {
        // 1. Nakit Bakiyeyi Bul
        Wallet wallet = getWalletOrThrow(userId, "TRY");
        BigDecimal cashBalance = wallet.getBalance();

        // 2. Hisseleri Bul ve Değerle
        List<Asset> assets = assetRepository.findAll().stream()
                .filter(a -> a.getUserId().equals(userId))
                .toList();

        List<AssetResponse> assetDTOs = assets.stream().map(asset -> {
            BigDecimal currentPrice = walletMarketService.getCurrentPrice(asset.getSymbol());
            BigDecimal totalValue = currentPrice.multiply(asset.getAvailableAmount().add(asset.getLockedAmount())); // Kilitlileri de sayıyoruz, onlar da bizim.

            return new AssetResponse(asset.getSymbol(), asset.getAvailableAmount(), currentPrice, totalValue);
        }).toList();

        // 3. Toplamları Hesapla
        BigDecimal totalAssetValue = assetDTOs.stream()
                .map(AssetResponse::totalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalWealth = cashBalance.add(totalAssetValue);

        return new PortfolioResponse(cashBalance, totalAssetValue, totalWealth, assetDTOs);
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

    // WalletServiceImpl içine ekle:
    public List<Asset> getUserAssets(Long userId) {
        return assetRepository.findAll().stream()
                .filter(a -> a.getUserId().equals(userId))
                .toList();
    }
}
