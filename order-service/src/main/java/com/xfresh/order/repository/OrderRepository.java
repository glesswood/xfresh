// product/repository/OrderRepository.java
package com.xfresh.order.repository;

import com.xfresh.order.entity.Order;
import feign.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUserId(Long userId, Pageable pageable);

    Optional<Order> findByUserIdAndRequestId(Long userId, String requestId);
    @Modifying
    @Query("update Order o set o.status = :to, o.updateTime = CURRENT_TIMESTAMP " +
            "where o.id = :id and o.status = :from")
    int updateStatusIfEquals(@Param("id") Long id,
                             @Param("from") int from,
                             @Param("to") int to);
}