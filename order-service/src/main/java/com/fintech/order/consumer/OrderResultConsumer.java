package com.fintech.order.consumer;

import com.fintech.common.event.WalletEvent;
import com.fintech.order.domain.enums.OrderStatus;
import com.fintech.order.model.Order;
import com.fintech.order.model.OrderOutbox;
import com.fintech.order.repository.OrderOutboxRepository;
import com.fintech.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderResultConsumer {

    private final OrderRepository orderRepository;
    private final OrderOutboxRepository outboxRepository;

    @KafkaListener(topics = "wallet-result-topic", groupId = "order-service-group")
    @Transactional
    public void consumeWalletResult(WalletEvent event) {
        log.info("Order Service received Wallet Result: {}", event);

        // 1. Audit Log (Gelen cevabı Order DB'sine de yazıyoruz ki iz sürebilelim)
        OrderOutbox outbox = OrderOutbox.builder()
                .orderId(event.orderId())
                .eventType("WALLET_RESPONSE_" + event.status())
                .payload(event.toString())
                .build();
        outboxRepository.save(outbox);

        // 2. Siparişi Güncelle
        Long orderId = Long.valueOf(event.orderId());
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if ("SUCCESS".equals(event.status())) {
            order.setStatus(OrderStatus.MATCHED);
        } else {
            order.setStatus(OrderStatus.REJECTED);
        }

        orderRepository.save(order);
    }
}
