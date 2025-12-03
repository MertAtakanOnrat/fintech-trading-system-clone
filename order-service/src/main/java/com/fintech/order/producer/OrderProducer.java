package com.fintech.order.producer;

import com.fintech.common.event.OrderCreatedEvent;
import com.fintech.order.model.OrderOutbox;
import com.fintech.order.repository.OrderOutboxRepository;
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
public class OrderProducer {

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    private final OrderOutboxRepository outboxRepository;

    // MANDATORY: Bu metodun çalışması için bir Transaction başlamış OLMALI.
    // (OrderService.createOrder metodu @Transactional olduğu için oradan transaction gelecek)
    @Transactional(propagation = Propagation.MANDATORY)
    public void sendMessage(OrderCreatedEvent event) {
        log.info("Saving Outbox & Sending Order Event: {}", event.orderId());

        // 1. DB Loglama (Outbox)
        OrderOutbox outbox = OrderOutbox.builder()
                .orderId(event.orderId())
                .eventType("ORDER_CREATED")
                .payload(event.toString())
                .build();
        outboxRepository.save(outbox);

        // 2. Kafka'ya At ve CEVABI BEKLE (.get())
        // Eğer Kafka kapalıysa burada hata fırlatılır -> Transaction Rollback olur -> Sipariş DB'den silinir.
        try {
            kafkaTemplate.send("order-created-topic", event).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Kafka send failed! Rolling back Order transaction. Error: {}", e.getMessage());
            throw new RuntimeException("Kafka unreachable", e);
        }
    }
}