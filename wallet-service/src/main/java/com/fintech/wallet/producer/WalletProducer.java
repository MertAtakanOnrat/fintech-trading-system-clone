package com.fintech.wallet.producer;

import com.fintech.common.event.WalletEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletProducer {

    private final KafkaTemplate<String, WalletEvent> kafkaTemplate;

    public void sendResult(String orderId, String status) {
        log.info("Sending Wallet Result: OrderId={}, Status={}", orderId, status);
        kafkaTemplate.send("wallet-result-topic", new WalletEvent(orderId, status));
    }
}
