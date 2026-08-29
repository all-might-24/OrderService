package com.ecommerceproject.orderservice.repositories;

import com.ecommerceproject.orderservice.models.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByUserId(Long userId);
    Page<Order> findByUserId(Long userId, Pageable pageable);

}
