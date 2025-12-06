package com.fintech.order.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintech.common.event.OrderCreatedEvent;
import com.fintech.order.model.OrderOutbox;
import com.fintech.order.repository.OrderOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisherJob {

    private final OrderOutboxRepository outboxRepository;
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;

    // Her 500 milisaniyede bir (yarım saniye) çalış
    @Scheduled(fixedRate = 500)
    public void publishPendingOrders() {
        // 1. Gönderilmemişleri bul
        List<OrderOutbox> pendingEvents = outboxRepository.findByProcessedFalse();

        if (pendingEvents.isEmpty()) return;

        log.info("Job found {} pending orders. Processing...", pendingEvents.size());

        for (OrderOutbox outbox : pendingEvents) {
            processSingleOutbox(outbox);
        }
    }

    // Her satırı ayrı transaction'da işle ki biri patlarsa diğerleri etkilenmesin
    @Transactional
    public void processSingleOutbox(OrderOutbox outbox) {
        try {
            // Sadece ORDER_CREATED tipindeki mesajları işliyoruz
            if (!"ORDER_CREATED".equals(outbox.getEventType())) {
                // Diğer tipleri (örn: WALLET_RESPONSE) şimdilik pas geç veya processed=true yap
                outbox.setProcessed(true);
                outboxRepository.save(outbox);
                return;
            }

            // 2. JSON String -> Java Object dönüşümü
            OrderCreatedEvent event = objectMapper.readValue(outbox.getPayload(), OrderCreatedEvent.class);

            // 3. Kafka'ya Gönder (Blocking send yapıyoruz ki emin olalım)
            // Eğer Kafka yoksa burada hata fırlatır ve catch'e düşer, processed=true OLMAZ.
            kafkaTemplate.send("order-created-topic", event).get();

            log.info("Job sent message to Kafka: {}", event.orderId());

            // 4. Başarılıysa işaretle
            outbox.setProcessed(true);
            outboxRepository.save(outbox);

        } catch (Exception e) {
            log.error("Job failed to process outbox id: {}. Will retry later.", outbox.getId(), e);
            // Hata fırlatmıyoruz ki döngü (loop) sonraki kayda geçebilsin.
            // Bu kayıt processed=false kalır, sonraki turda tekrar denenir.
        }
    }
}