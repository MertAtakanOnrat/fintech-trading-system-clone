package com.fintech.matching.service;

import com.fintech.common.event.TradeExecutedEvent;
import com.fintech.matching.engine.OrderBook;
import com.fintech.matching.model.LimitOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class MatchingService {

    private final Map<String, OrderBook> orderBooks = new HashMap<>();
    private final KafkaTemplate<String, TradeExecutedEvent> kafkaTemplate;

    public void processOrder(LimitOrder incomingOrder) {
        OrderBook book = orderBooks.computeIfAbsent(incomingOrder.getSymbol(), OrderBook::new);

        log.info("Processing {} order for {} @ {}", incomingOrder.getSide(), incomingOrder.getSymbol(), incomingOrder.getPrice());

        while (incomingOrder.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            LimitOrder bestMatch = book.peekBestMatch(incomingOrder.getSide());

            // 1. Karşı tarafta emir yoksa -> Deftere yaz, çık.
            if (bestMatch == null) {
                book.addOrder(incomingOrder);
                log.info("No match found. Order added to book.");
                break;
            }

            // 2. Fiyat Kontrolü
            boolean isPriceMatch;
            if ("BUY".equals(incomingOrder.getSide())) {
                // Alıcıysam, satıcının fiyatı benimkinden düşük veya eşit olmalı
                isPriceMatch = incomingOrder.getPrice().compareTo(bestMatch.getPrice()) >= 0;
            } else {
                // Satıcıysam, alıcının fiyatı benimkinden yüksek veya eşit olmalı
                isPriceMatch = incomingOrder.getPrice().compareTo(bestMatch.getPrice()) <= 0;
            }

            if (!isPriceMatch) {
                book.addOrder(incomingOrder);
                log.info("Price mismatch. Order added to book.");
                break;
            }

            // 3. EŞLEŞME (TRADE) GERÇEKLEŞİYOR 🔥
            BigDecimal tradeAmount = incomingOrder.getAmount().min(bestMatch.getAmount());
            BigDecimal tradePrice = bestMatch.getPrice(); // Maker (Bekleyen) fiyatından işlem olur!

            log.info("MATCHED! {} shares @ {}", tradeAmount, tradePrice);

            // GÜNCELLENMİŞ EVENT OLUŞTURMA
            TradeExecutedEvent tradeEvent = new TradeExecutedEvent(
                    incomingOrder.getSymbol(),
                    "BUY".equals(incomingOrder.getSide()) ? incomingOrder.getOrderId() : bestMatch.getOrderId(),
                    "SELL".equals(incomingOrder.getSide()) ? incomingOrder.getOrderId() : bestMatch.getOrderId(),
                    "BUY".equals(incomingOrder.getSide()) ? incomingOrder.getUserId() : bestMatch.getUserId(), // Buyer ID
                    "SELL".equals(incomingOrder.getSide()) ? incomingOrder.getUserId() : bestMatch.getUserId(), // Seller ID
                    tradePrice,
                    tradeAmount,
                    System.currentTimeMillis()
            );

            kafkaTemplate.send("trade-executed-topic", tradeEvent);

            // 4. Miktarları Güncelle (Mutable Class Avantajı)
            incomingOrder.setAmount(incomingOrder.getAmount().subtract(tradeAmount));
            bestMatch.setAmount(bestMatch.getAmount().subtract(tradeAmount));

            // Eğer defterdeki emir bittiyse sil
            if (bestMatch.getAmount().compareTo(BigDecimal.ZERO) == 0) {
                book.removeOrder(bestMatch);
            }
        }
    }
}