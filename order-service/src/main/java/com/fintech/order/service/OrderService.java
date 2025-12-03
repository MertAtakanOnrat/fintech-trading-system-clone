package com.fintech.order.service;

import com.fintech.common.event.OrderCreatedEvent;
import com.fintech.order.domain.enums.OrderStatus;
import com.fintech.order.dto.CreateOrderRequest;
import com.fintech.order.model.Order;
import com.fintech.order.producer.OrderProducer;
import com.fintech.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderProducer orderProducer;
    // private final StringRedisTemplate redisTemplate; <-- BUNU SİLDİK
    private final MarketPriceService marketPriceService; // <-- BUNU EKLEDİK

    @Transactional
    public Long createOrder(CreateOrderRequest request) {
        // Fiyat Belirleme Mantığı
        BigDecimal finalPrice = request.price();

        if (finalPrice == null) {
            // Detayları (Redis key, parsing vs.) bilmiyoruz, sadece fiyatı istiyoruz.
            finalPrice = marketPriceService.fetchCurrentPrice(request.symbol());
            log.info("Market Order detected. Used current price for {}: {}", request.symbol(), finalPrice);
        } else {
            log.info("Limit Order detected. User requested price: {}", finalPrice);
        }

        // 1. Emri Veritabanına Kaydet
        Order order = Order.builder()
                .userId(request.userId())
                .symbol(request.symbol())
                .side(request.side())
                .amount(request.amount())
                .price(finalPrice)
                .status(OrderStatus.PENDING)
                .build();

        Order savedOrder = orderRepository.save(order);

        // 2. Event Oluştur
        OrderCreatedEvent event = new OrderCreatedEvent(
                savedOrder.getId().toString(),
                savedOrder.getUserId(),
                savedOrder.getSymbol(),
                savedOrder.getAmount(),
                savedOrder.getPrice(),
                savedOrder.getSide().toString()
        );

        // 3. Kafka'ya Fırlat
        orderProducer.sendMessage(event);

        return savedOrder.getId();
    }
}