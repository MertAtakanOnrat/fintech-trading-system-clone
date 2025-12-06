package com.fintech.order.repository;

import com.fintech.order.model.OrderOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderOutboxRepository extends JpaRepository<OrderOutbox, Long> {

    // Henüz işlenmemiş kayıtları getir
    List<OrderOutbox> findByProcessedFalse();
}