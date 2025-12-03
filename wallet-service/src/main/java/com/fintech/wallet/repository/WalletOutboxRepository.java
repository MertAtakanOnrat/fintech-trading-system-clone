package com.fintech.wallet.repository;

import com.fintech.wallet.model.WalletOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletOutboxRepository extends JpaRepository<WalletOutbox, Long> {
}