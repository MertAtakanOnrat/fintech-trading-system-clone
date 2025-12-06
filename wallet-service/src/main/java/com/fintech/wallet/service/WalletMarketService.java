package com.fintech.wallet.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletMarketService {

    private final StringRedisTemplate redisTemplate;

    public BigDecimal getCurrentPrice(String symbol) {
        String priceStr = redisTemplate.opsForValue().get("PRICE:" + symbol);

        // Eğer Redis'te fiyat yoksa (Market kapalıysa vs.) varsayılan 0 veya hata dönebiliriz.
        // Şimdilik güvenli olsun diye 0 dönmeyelim, null kontrolü yapalım.
        if (priceStr == null) {
            return BigDecimal.ZERO;
        }

        return new BigDecimal(priceStr);
    }
}