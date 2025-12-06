package com.fintech.order.producer;

import com.fasterxml.jackson.databind.ObjectMapper; // JSON kütüphanesi
import com.fintech.common.event.OrderCreatedEvent;
import com.fintech.order.model.OrderOutbox;
import com.fintech.order.repository.OrderOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderProducer {

    private final OrderOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper; // Spring Boot otomatik inject eder

    // KafkaTemplate ARTIK YOK! Sadece DB'ye yazıyoruz. Hız budur.
    @Transactional(propagation = Propagation.MANDATORY)
    public void sendMessage(OrderCreatedEvent event) {
        log.info("Saving Order to Outbox (Async Prep): {}", event.orderId());

        try {
            // Gerçek JSON dönüşümü
            String payloadJson = objectMapper.writeValueAsString(event);

            OrderOutbox outbox = OrderOutbox.builder()
                    .orderId(event.orderId())
                    .eventType("ORDER_CREATED")
                    .payload(payloadJson)
                    .processed(false) // Gönderilmedi olarak işaretle
                    .build();

            outboxRepository.save(outbox);

        } catch (Exception e) {
            // JSON hatası olursa işlemi iptal et
            log.error("Error serializing event", e);
            throw new RuntimeException(e);
        }
    }
}