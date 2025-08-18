package com.xfresh.order.domain.repository;

import com.xfresh.order.domain.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository  extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);
}