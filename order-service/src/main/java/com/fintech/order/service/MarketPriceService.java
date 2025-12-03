package com.fintech.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketPriceService {

    private final StringRedisTemplate redisTemplate;

    public BigDecimal fetchCurrentPrice(String symbol) {
        String priceStr = redisTemplate.opsForValue().get("PRICE:" + symbol);

        if (priceStr == null) {
            log.error("Price data not found in Redis for symbol: {}", symbol);
            throw new RuntimeException("Market data not available for symbol: " + symbol);
        }

        return new BigDecimal(priceStr);
    }
}
