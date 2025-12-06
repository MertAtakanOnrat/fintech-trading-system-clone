package com.fintech.matching.consumer;

import com.fintech.common.event.OrderCreatedEvent;
import com.fintech.matching.model.LimitOrder;
import com.fintech.matching.service.MatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final MatchingService matchingService;

    @KafkaListener(topics = "order-created-topic", groupId = "matching-engine-group")
    public void consume(OrderCreatedEvent event) {
        log.info("Matching Engine received Order: {}", event);

        // Event -> LimitOrder (Internal Model) Dönüşümü
        LimitOrder order = new LimitOrder(
                event.orderId(),
                event.userId(),
                event.symbol(),
                event.amount(),
                event.price(),
                event.side(),
                System.currentTimeMillis()
        );

        matchingService.processOrder(order);
    }
}