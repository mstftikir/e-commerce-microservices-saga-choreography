package com.taltech.ecommerce.orderservice.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.taltech.ecommerce.orderservice.event.OrderEvent;
import com.taltech.ecommerce.orderservice.service.OrderService;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final OrderService service;
    private final ObservationRegistry observationRegistry;

    @KafkaListener(topics = "orderCompleted")
    public void receiveOrderCompleted(OrderEvent event) {
        Observation.createNotStarted("order-completed-received", this.observationRegistry)
            .observe(() -> {
                log.info("Order completed event received");
                service.orderCompleted(event);
            });
    }

    @KafkaListener(topics = "orderFailed")
    public void receiveOrderFailed(OrderEvent event) {
        Observation.createNotStarted("order-failed-received", this.observationRegistry)
            .observe(() -> {
                log.info("Order failed event received");
                service.paymentSaveFailed(event);
            });
    }
}
