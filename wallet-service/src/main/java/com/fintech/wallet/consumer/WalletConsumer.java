package com.fintech.wallet.consumer;

import com.fintech.common.event.TradeExecutedEvent;
import com.fintech.common.exception.InsufficientBalanceException;
import com.fintech.wallet.producer.WalletProducer;
import com.fintech.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletConsumer {

    private final WalletService walletService;
    private final WalletProducer walletProducer;

    @KafkaListener(topics = "trade-executed-topic", groupId = "wallet-service-group")
    @Transactional
    public void consumeTrade(TradeExecutedEvent event) {
        log.info("Trade Event Received: {} shares of {} @ {}", event.amount(), event.symbol(), event.price());

        try {
            // 1. ALICI TARAFI (Buyer): Para Ver -> Hisse Al
            // event.buyerId() metodunu kullanıyoruz (userId yok artık)
            log.info("Processing BUYER side (User: {})", event.buyerId());

            walletService.executeBuyOrder(
                    event.buyerId(),
                    event.symbol(),
                    event.amount(),
                    event.price()
            );

            // Alıcı siparişine (buyOrderId) SUCCESS dön
            walletProducer.sendResult(event.buyOrderId(), "SUCCESS");


            // 2. SATICI TARAFI (Seller): Hisse Ver -> Para Al
            // event.sellerId() metodunu kullanıyoruz
            log.info("Processing SELLER side (User: {})", event.sellerId());

            walletService.executeSellOrder(
                    event.sellerId(),
                    event.symbol(),
                    event.amount(),
                    event.price()
            );

            // Satıcı siparişine (sellOrderId) SUCCESS dön
            walletProducer.sendResult(event.sellOrderId(), "SUCCESS");

            log.info("Trade Settlement Completed Successfully ✅");

        } catch (InsufficientBalanceException e) {
            // Burada kritik bir durum var: Matching Engine eşleştirdi ama cüzdanda para/hisse yok.
            // Bu "Settlement Failure" (Takas Hatası) durumudur.
            log.error("Settlement Business Error: {}", e.getMessage());

            // Hata alan tarafa FAILED dönmek lazım.
            // Basitlik adına şimdilik her iki siparişe de FAILED dönüyoruz (veya loglayıp geçiyoruz).
            // Gerçek sistemde burada "Compensation" (Telafi) işlemi yapılır.
            walletProducer.sendResult(event.buyOrderId(), "FAILED");
            walletProducer.sendResult(event.sellOrderId(), "FAILED");

        } catch (Exception e) {
            log.error("System error during settlement: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}