package com.fintech.wallet.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "assets", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "symbol"}) // Bir kullanıcının bir hissesi tek satırda tutulur
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String symbol; // ASELS, THYAO

    @Column(nullable = false)
    private BigDecimal availableAmount; // Satılabilir miktar (Adet)

    @Column(nullable = false)
    private BigDecimal lockedAmount; // Emirde bekleyen (Bloke) miktar

    // Yardımcı metod: Toplam varlık (Kullanılabilir + Bloke)
    public BigDecimal getTotalAmount() {
        return availableAmount.add(lockedAmount);
    }
}
