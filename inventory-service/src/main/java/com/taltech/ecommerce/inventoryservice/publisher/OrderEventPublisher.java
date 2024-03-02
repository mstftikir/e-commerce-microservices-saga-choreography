package com.taltech.ecommerce.inventoryservice.publisher;

import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import com.taltech.ecommerce.inventoryservice.event.OrderEvent;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private final ObservationRegistry observationRegistry;

    public void publishInventoryUpdated(OrderEvent orderEvent) {
        publishEvent("inventoryUpdatedTopic", "inventory-updated-sent", orderEvent);
    }

    public void publishInventoryUpdateFailed(OrderEvent orderEvent) {
        publishEvent("inventoryUpdateFailedTopic", "inventory-update-failed-sent", orderEvent);
    }

    public void publishInventoryRollbacked(OrderEvent orderEvent) {
        publishEvent("inventoryRollbackedTopic", "inventory-rollbacked-sent", orderEvent);
    }

    public void publishInventoryRollbackFailed(OrderEvent orderEvent) {
        publishEvent("inventoryRollbackFailedTopic", "inventory-rollback-failed-sent", orderEvent);
    }

    private void publishEvent(String topic, String observationName, OrderEvent orderEvent) {
        log.info("Publishing inventory event to '{}'", topic);

        try {
            Observation.createNotStarted(observationName, this.observationRegistry).observe(() -> {
                CompletableFuture<SendResult<String, OrderEvent>> future = kafkaTemplate.send(topic, orderEvent);
                return future.handle((result, throwable) -> CompletableFuture.completedFuture(result));
            });
        } catch (Exception exception) {
            log.error("Exception occurred while sending message to kafka, exception message: {}", exception.getMessage());
        }
    }
}

