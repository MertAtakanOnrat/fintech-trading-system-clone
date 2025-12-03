package com.fintech.wallet.producer;

import com.fintech.common.event.WalletEvent;
import com.fintech.wallet.model.WalletOutbox;
import com.fintech.wallet.repository.WalletOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletProducer {

    private final KafkaTemplate<String, WalletEvent> kafkaTemplate;
    private final WalletOutboxRepository outboxRepository;

    // MANDATORY: Bu metodun mutlaka halihazırda başlamış bir transaction içinde çağrılması gerekir.
    @Transactional(propagation = Propagation.MANDATORY)
    public void sendResult(String orderId, String status) {
        log.info("Saving to Outbox & Sending Wallet Result: OrderId={}, Status={}", orderId, status);

        // 1. DB Outbox Kaydı
        WalletOutbox outbox = WalletOutbox.builder()
                .orderId(orderId)
                .eventType("WALLET_" + status)
                .payload("{\"status\": \"" + status + "\"}")
                .build();

        outboxRepository.save(outbox);

        // 2. Kafka'ya At ve CEVABI BEKLE (.get())
        try {
            kafkaTemplate.send("wallet-result-topic", new WalletEvent(orderId, status)).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Kafka send failed! Initiating Rollback. Error: {}", e.getMessage());
            // Hatayı fırlat ki Transaction Rollback olsun
            throw new RuntimeException("Kafka send failed", e);
        }
    }
}
