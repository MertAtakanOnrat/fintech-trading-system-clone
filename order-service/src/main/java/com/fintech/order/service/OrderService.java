package com.fintech.order.service;

import com.fintech.common.event.OrderCreatedEvent;
import com.fintech.order.domain.enums.OrderStatus;
import com.fintech.order.dto.CreateOrderRequest;
import com.fintech.order.model.Order;
import com.fintech.order.producer.OrderProducer;
import com.fintech.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderProducer orderProducer;

    @Transactional
    public Long createOrder(CreateOrderRequest request){
        // 1. Emri veri tabanına kayıt et
        Order order = Order.builder()
                .userId(request.userId())
                .symbol(request.symbol())
                .side(request.side())
                .amount(request.amount())
                .price(request.price())
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
                savedOrder.getSide().toString() // BUY/SELL Enum -> String
        );

        // 3. Kafka'ya Fırlat
        orderProducer.sendMessage(event);

        return savedOrder.getId();

    }
}
