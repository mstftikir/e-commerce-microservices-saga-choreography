package com.taltech.ecommerce.orderservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taltech.ecommerce.orderservice.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderEventStatusId(String eventId);
}
