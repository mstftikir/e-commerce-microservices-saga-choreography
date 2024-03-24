package com.taltech.ecommerce.orderservice.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.taltech.ecommerce.orderservice.dto.OrderDto;
import com.taltech.ecommerce.orderservice.dto.user.UserDto;
import com.taltech.ecommerce.orderservice.event.OrderEvent;
import com.taltech.ecommerce.orderservice.mapper.OrderMapper;
import com.taltech.ecommerce.orderservice.model.Order;
import com.taltech.ecommerce.orderservice.model.OrderEventStatus;
import com.taltech.ecommerce.orderservice.publisher.OrderEventPublisher;
import com.taltech.ecommerce.orderservice.repository.OrderRepository;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Retryable
@Slf4j
public class OrderService {

    private final OrderRepository repository;
    private final OrderMapper mapper;
    private final WebClient.Builder webClientBuilder;
    private final ObservationRegistry observationRegistry;

    private final OrderEventPublisher orderEventPublisher;

    @Value("${user.service.url}")
    private String userServiceUrl;

    public void placeOrder(Order order) {
        validations(order);

        OrderEventStatus orderEventStatus = new OrderEventStatus();
        orderEventStatus.setId(UUID.randomUUID().toString());
        order.setOrderEventStatus(orderEventStatus);

        addDates(order);
        repository.save(order);

        startOrder(order);
    }

    public void orderCompleted(OrderEvent orderEvent) {
        Order receivedOrder = mapper.toModel(orderEvent.getOrder());
        Order order = findOrderByEventId(receivedOrder.getOrderEventStatus().getId());

        order.setPaymentCode(orderEvent.getOrder().getPaymentCode());

        order.getOrderEventStatus().setInventoryStatus(receivedOrder.getOrderEventStatus().getInventoryStatus());
        order.getOrderEventStatus().setChartStatus(receivedOrder.getOrderEventStatus().getChartStatus());
        order.getOrderEventStatus().setPaymentStatus(receivedOrder.getOrderEventStatus().getPaymentStatus());
        order.setUpdateDate(LocalDateTime.now());

        repository.saveAndFlush(order);
    }

    public void orderFailed(OrderEvent orderEvent) {
        Order receivedOrder = mapper.toModel(orderEvent.getOrder());
        Order order = findOrderByEventId(receivedOrder.getOrderEventStatus().getId());

        order.getOrderEventStatus().setInventoryStatus(receivedOrder.getOrderEventStatus().getInventoryStatus());
        order.getOrderEventStatus().setChartStatus(receivedOrder.getOrderEventStatus().getChartStatus());
        order.getOrderEventStatus().setPaymentStatus(receivedOrder.getOrderEventStatus().getPaymentStatus());
        order.setUpdateDate(LocalDateTime.now());

        repository.saveAndFlush(order);
    }

    private void validations(Order order) {
        validateUser(order);
        // other validations
    }

    private void validateUser(Order order) {
        Observation userServiceObservation = Observation.createNotStarted("user-service-validation", this.observationRegistry);
        userServiceObservation.lowCardinalityKeyValue("call", "user-service");
        userServiceObservation.observe(() -> {
            UserDto userResponseDto = webClientBuilder.build().get()
                .uri(userServiceUrl + order.getUserId())
                .retrieve()
                .bodyToMono(UserDto.class)
                .block();
            if(userResponseDto == null) {
                throw new EntityNotFoundException(String.format("User '%s' is not found", order.getUserId()));
            }
        });
    }

    private void startOrder(Order order) {
        OrderDto orderDto = mapper.toDto(order);
        OrderEvent orderEvent = OrderEvent.builder().order(orderDto).build();
        orderEventPublisher.publishUpdateInventory(orderEvent);
    }

    private void addDates(Order order) {
        order.setInsertDate(LocalDateTime.now());
        order.setUpdateDate(LocalDateTime.now());

        order.getOrderItems().forEach(paymentItem -> {
            paymentItem.setInsertDate(LocalDateTime.now());
            paymentItem.setUpdateDate(LocalDateTime.now());
        });
    }

    private Order findOrderByEventId(String eventId) {
        return repository.findByOrderEventStatusId(eventId)
            .orElseThrow(() -> new EntityNotFoundException(String.format("Order not found by eventId '%s'",
                eventId)));
    }
}
