package com.taltech.ecommerce.paymentservice.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.taltech.ecommerce.paymentservice.event.OrderEvent;
import com.taltech.ecommerce.paymentservice.service.PaymentService;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final PaymentService service;
    private final ObservationRegistry observationRegistry;

    @KafkaListener(topics = "savePaymentTopic")
    public void receiveSavePayment(OrderEvent orderEvent) {
        Observation.createNotStarted("save-payment-received", this.observationRegistry)
            .observe(() -> {
                log.info("Save order payment event '{}' received", orderEvent.getOrder().getOrderEventStatus().getId());
                service.commitSave(orderEvent);
            });
    }

    @KafkaListener(topics = "rollbackPaymentTopic")
    public void receiveRollbackPayment(OrderEvent orderEvent) {
        Observation.createNotStarted("rollback-payment-received", this.observationRegistry)
            .observe(() -> {
                log.info("Rollback order payment event '{}' received", orderEvent.getOrder().getOrderEventStatus().getId());
                service.rollbackSave(orderEvent);
            });
    }
}
