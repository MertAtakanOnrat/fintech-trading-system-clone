package com.fintech.order.producer;

import com.fintech.common.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j // Loglama için (Lombok)
public class OrderProducer {

    // Kafka'ya mesaj atan Spring aracı
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public void sendMessage(OrderCreatedEvent event) {
        log.info("Order event sent to Kafka: {}", event);

        // Mesajı oluşturuyoruz
        Message<OrderCreatedEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, "order-created-topic")
                .build();

        // Ve fırlatıyoruz! 🚀
        kafkaTemplate.send(message);
    }
}
