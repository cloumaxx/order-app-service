package com.uni.sabana.order_app.infrastructure.repostory;

import com.uni.sabana.order_app.domain.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
