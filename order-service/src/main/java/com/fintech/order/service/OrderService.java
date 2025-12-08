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
    private final MarketPriceService marketPriceService;
    private final WalletGrpcClient walletGrpcClient;

    @Transactional
    public Long createOrder(CreateOrderRequest request) {
        // 1. ÖNCE FİYATI BELİRLE (Sıralamayı düzelttik)
        BigDecimal finalPrice = request.price();

        if (finalPrice == null) {
            // Market Emir: Fiyatı Redis'ten çek
            finalPrice = marketPriceService.fetchCurrentPrice(request.symbol());
            log.info("Market Order detected. Used current price for {}: {}", request.symbol(), finalPrice);
        } else {
            // Limit Emir: Kullanıcının istediği fiyat
            log.info("Limit Order detected. User requested price: {}", finalPrice);
        }

        // 2. ŞİMDİ BAKİYE KONTROLÜ YAP (gRPC)
        // Artık finalPrice elimizde olduğu için hata vermez.
        if ("BUY".equals(request.side().name())) {
            double totalAmount = finalPrice.multiply(request.amount()).doubleValue();

            boolean hasFunds = walletGrpcClient.hasSufficientFunds(request.userId(), totalAmount);

            if (!hasFunds) {
                log.warn("Pre-check failed: Insufficient funds via gRPC for User {}", request.userId());
                throw new RuntimeException("Insufficient funds (gRPC check failed)");
            }
        }

        // 3. Emri Veritabanına Kaydet
        Order order = Order.builder()
                .userId(request.userId())
                .symbol(request.symbol())
                .side(request.side())
                .amount(request.amount())
                .price(finalPrice)
                .status(OrderStatus.PENDING)
                .build();

        Order savedOrder = orderRepository.save(order);

        // 4. Event Oluştur
        OrderCreatedEvent event = new OrderCreatedEvent(
                savedOrder.getId().toString(),
                savedOrder.getUserId(),
                savedOrder.getSymbol(),
                savedOrder.getAmount(),
                savedOrder.getPrice(),
                savedOrder.getSide().toString()
        );

        // 5. Kafka'ya Fırlat (Outbox)
        orderProducer.sendMessage(event);

        return savedOrder.getId();
    }
}