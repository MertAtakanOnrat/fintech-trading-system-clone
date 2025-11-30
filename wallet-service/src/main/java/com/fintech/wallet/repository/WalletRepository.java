package com.fintech.wallet.repository;

import com.fintech.wallet.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    // Kullanıcının belirli bir para birimindeki cüzdanını bul
    // Örn: User 1'in TRY cüzdanını getir.
    Optional<Wallet> findByUserIdAndCurrency(Long userId, String currency);
    // Sadece var mı yok mu kontrolü için (Performanslı)
    boolean existsByUserIdAndCurrency(Long userId, String currency);
}
