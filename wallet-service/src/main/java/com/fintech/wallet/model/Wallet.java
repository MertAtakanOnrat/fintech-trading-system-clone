package com.fintech.wallet.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallets", indexes = {
        @Index(name = "idx_user_currency", columnList = "user_id, currency", unique = true)
}) // Bir kullanıcının aynı para biriminden sadece 1 cüzdanı olabilir (Unique Constraint)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 3)
    private String currency; // TRY, USD, EUR

    @Column(nullable = false)
    private BigDecimal balance;

    // --- SENIOR MÜLAKAT SORUSU: Optimistic Locking ---
    // Bu alan veritabanındaki satırın versiyonunu tutar.
    // Eğer iki işlem aynı anda bakiyeyi değiştirmeye çalışırsa;
    // İlk işlem versiyonu 1'den 2'ye çeker.
    // İkinci işlem elindeki versiyon 1 olduğu için "Row was updated or deleted" hatası alır.
    // Böylece bakiye tutarsızlığı (Lost Update Problem) önlenir.
    @Version
    private Long version;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (balance == null) {
            balance = BigDecimal.ZERO;
        }
    }
}
