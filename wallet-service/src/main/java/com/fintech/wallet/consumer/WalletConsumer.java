package com.fintech.wallet.consumer;

import com.fintech.common.event.OrderCreatedEvent;
import com.fintech.wallet.producer.WalletProducer;
import com.fintech.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletConsumer {

    private final WalletService walletService;
    private final WalletProducer walletProducer; // Yeni sınıfı kullanıyoruz

    @KafkaListener(topics = "order-created-topic", groupId = "wallet-service-group") // İsim güncellendi
    public void consume(OrderCreatedEvent event) {
        log.info("Wallet Service received Order Event: {}", event);

        try {
            BigDecimal totalAmount = event.price().multiply(event.amount());

            walletService.withdraw(event.userId(), "TRY", totalAmount);

            // Başarılıysa Producer'a devret
            walletProducer.sendResult(event.orderId(), "SUCCESS");

        } catch (Exception e) {
            log.error("Failed to process order {}: {}", event.orderId(), e.getMessage());

            // Hataysa Producer'a devret
            walletProducer.sendResult(event.orderId(), "FAILED");
        }
    }
}
