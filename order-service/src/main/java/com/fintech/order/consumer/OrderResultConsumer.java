package com.fintech.order.consumer;

import com.fintech.common.event.WalletEvent;
import com.fintech.order.domain.enums.OrderStatus;
import com.fintech.order.model.Order;
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

    @KafkaListener(topics = "wallet-result-topic", groupId = "order-service-group")
    @Transactional
    public void consumeWalletResult(WalletEvent event) {
        log.info("Order Service received Wallet Result: {}", event);

        Long orderId = Long.valueOf(event.orderId());

        // Siparişi bul
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Durumu Güncelle
        if ("SUCCESS".equals(event.status())) {
            order.setStatus(OrderStatus.MATCHED); // veya VALIDATED
            log.info("Order {} is now MATCHED", orderId);
        } else {
            order.setStatus(OrderStatus.REJECTED);
            log.info("Order {} is REJECTED due to insufficient funds", orderId);
        }

        orderRepository.save(order);
    }
}
