package com.fintech.matching.engine;

import com.fintech.matching.model.LimitOrder;
import lombok.Getter;

import java.util.PriorityQueue;

@Getter
public class OrderBook {
    private final String symbol;

    // Alış Emirleri (Otomatik Sıralanır: En Yüksek Fiyat Tepede)
    private final PriorityQueue<LimitOrder> buyOrders;

    // Satış Emirleri (Otomatik Sıralanır: En Düşük Fiyat Tepede)
    private final PriorityQueue<LimitOrder> sellOrders;

    public OrderBook(String symbol) {
        this.symbol = symbol;
        this.buyOrders = new PriorityQueue<>(); // LimitOrder içindeki compareTo metodunu kullanır
        this.sellOrders = new PriorityQueue<>();
    }

    public void addOrder(LimitOrder order) {
        if ("BUY".equals(order.getSide())) {
            buyOrders.add(order);
        } else {
            sellOrders.add(order);
        }
    }

    // En iyi teklifi getir (Eşleşme kontrolü için)
    public LimitOrder peekBestMatch(String side) {
        if ("BUY".equals(side)) {
            return sellOrders.peek(); // Alıcıysan, en ucuz satıcıya bak
        } else {
            return buyOrders.peek(); // Satıcıysan, en pahalı alıcıya bak
        }
    }

    // Eşleşen emri kuyruktan sil (Trade gerçekleşti)
    public void removeOrder(LimitOrder order) {
        if ("BUY".equals(order.getSide())) {
            buyOrders.remove(order);
        } else {
            sellOrders.remove(order);
        }
    }
}