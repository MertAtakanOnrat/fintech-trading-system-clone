package com.fintech.order.controller;

import com.fintech.order.dto.CreateOrderRequest;
import com.fintech.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody CreateOrderRequest request){
        Long orderId = orderService.createOrder(request);
        return ResponseEntity.ok("Order received with ID: " + orderId + ". Processing started.");
    }
}
