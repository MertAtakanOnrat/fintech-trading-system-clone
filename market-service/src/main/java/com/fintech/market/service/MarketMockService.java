package com.fintech.market.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate; // EKLENECEK

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import jakarta.annotation.PostConstruct; // javax.annotation yerine jakarta (SpringBoot 3+)


@Service
@RequiredArgsConstructor // Lombok constructor'ı otomatik oluşturur (redisTemplate için)
@Slf4j
public class MarketMockService {

    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate; // EKLENDİ (Yayın Aracı)

    // Hisse senetleri ve hafızadaki son fiyatları
    private final Map<String, BigDecimal> stocks = new ConcurrentHashMap<>();

    // Uygulama ayağa kalktıktan hemen sonra 1 kere çalışır
    @PostConstruct
    public void init() {
        stocks.put("THYAO", new BigDecimal("250.00"));
        stocks.put("ASELS", new BigDecimal("45.00"));
        stocks.put("BTC", new BigDecimal("3000000.00")); // TL bazlı
        stocks.put("ETH", new BigDecimal("100000.00"));
        log.info("Market Data Initialized with stocks: {}", stocks.keySet());
    }

    // Her 1 saniyede (1000ms) bir çalışır ve fiyatı değiştirir
    @Scheduled(fixedRate = 1000)
    public void updatePrices() {
        stocks.forEach((symbol, currentPrice) -> {
            // %1 aşağı veya yukarı rastgele oyna
            double changePct = (ThreadLocalRandom.current().nextDouble() * 0.02) - 0.01;
            BigDecimal change = currentPrice.multiply(BigDecimal.valueOf(changePct));
            BigDecimal newPrice = currentPrice.add(change).setScale(2, RoundingMode.HALF_UP);

            // 1. Kendi hafızasını güncelle
            stocks.put(symbol, newPrice);

            // 2. REDIS'E YAZ (Order Service buradan okuyacak)
            // Key: "PRICE:THYAO" -> Value: "252.30"
            redisTemplate.opsForValue().set("PRICE:" + symbol, newPrice.toString());

            // 3. WEBSOCKET İLE YAYINLA (YENİ) 📡
            // Kanal: /topic/prices
            // Mesaj: {"symbol": "THYAO", "price": 250.50} (Basit JSON String)
            String jsonUpdate = String.format("{\"symbol\":\"%s\", \"price\":%s}", symbol, newPrice);
            messagingTemplate.convertAndSend("/topic/prices", jsonUpdate);

            log.info("Updated Price: {} -> {}", symbol, newPrice);
        });
    }
}