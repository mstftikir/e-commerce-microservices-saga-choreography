package com.taltech.ecommerce.inventoryservice.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.taltech.ecommerce.inventoryservice.event.OrderEvent;
import com.taltech.ecommerce.inventoryservice.service.InventoryService;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventListener {

    private final InventoryService service;
    private final ObservationRegistry observationRegistry;

    @KafkaListener(topics = "updateInventoryTopic")
    public void receiveUpdateInventory(OrderEvent orderEvent) {
        Observation.createNotStarted("update-inventory-received", this.observationRegistry)
            .observe(() -> {
                log.info("Update inventory event '{}' received", orderEvent.getOrder().getOrderEventStatus().getId());
                service.commitUpdate(orderEvent);
            });
    }

    @KafkaListener(topics = "rollbackInventoryTopic")
    public void receiveRollbackInventory(OrderEvent orderEvent) {
        Observation.createNotStarted("rollback-inventory-received", this.observationRegistry)
            .observe(() -> {
                log.info("Rollback inventory event '{}' received", orderEvent.getOrder().getOrderEventStatus().getId());
                service.rollbackUpdate(orderEvent);
            });
    }
}
