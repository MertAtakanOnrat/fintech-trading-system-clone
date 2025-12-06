package com.fintech.wallet.repository;

import com.fintech.wallet.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    Optional<Asset> findByUserIdAndSymbol(Long userId, String symbol);
}