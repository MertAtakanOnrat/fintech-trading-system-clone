package com.fintech.market.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // İstemcilerin abone olacağı ana başlık (Örn: /topic/prices)
        config.enableSimpleBroker("/topic");
        // İstemciden gelen mesajların öneki (Bizde tek yönlü olacak ama standarttır)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket bağlantı noktası
        registry.addEndpoint("/ws-market")
                .setAllowedOriginPatterns("*") // CORS izni (Test html'i için şart)
                .withSockJS(); // Fallback desteği
    }
}