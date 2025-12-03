package com.fintech.wallet.consumer;

import com.fintech.common.event.OrderCreatedEvent;
import com.fintech.common.exception.InsufficientBalanceException;
import com.fintech.wallet.producer.WalletProducer;
import com.fintech.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletConsumer {

    private final WalletService walletService;
    private final WalletProducer walletProducer;

    @KafkaListener(topics = "order-created-topic", groupId = "wallet-service-group")
    @Transactional
    public void consume(OrderCreatedEvent event) {
        log.info("Wallet Service received Order Event: {}", event);

        try {
            BigDecimal totalAmount = event.price().multiply(event.amount());

            walletService.withdraw(event.userId(), "TRY", totalAmount);

            walletProducer.sendResult(event.orderId(), "SUCCESS");

        } catch (InsufficientBalanceException e) {
            // İŞTE ÇÖZÜM: Bu bir Checked Exception olduğu için Transaction Rollback OLMAZ.
            // Yani "FAILED" mesajını attığımızda DB'ye de başarıyla commit edilecek.
            log.error("Business logic error (Insufficient Balance) for order {}: {}", event.orderId(), e.getMessage());
            walletProducer.sendResult(event.orderId(), "FAILED");

        } catch (Exception e) {
            // Diğer beklenmedik hatalar (DB çöktü vs.) hala rollback yapar ve retry olur.
            log.error("System error processing order {}: {}", event.orderId(), e.getMessage());
            // Burada FAILED atmıyoruz, çünkü sistem hatası var, Kafka tekrar denesin istiyoruz.
            throw new RuntimeException(e);
        }
    }
}
