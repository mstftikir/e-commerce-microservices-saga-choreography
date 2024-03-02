package com.taltech.ecommerce.orderservice.publisher;

import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import com.taltech.ecommerce.orderservice.event.OrderEvent;

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

    public void publishUpdateInventory(OrderEvent event) {
        log.info("Publishing order event to 'updateInventoryTopic'");

        try {
            Observation.createNotStarted("update-inventory-sent", this.observationRegistry).observe(() -> {
                CompletableFuture<SendResult<String, OrderEvent>> future = kafkaTemplate.send("updateInventoryTopic", event);
                return future.handle((result, throwable) -> CompletableFuture.completedFuture(result));
            });
        } catch (Exception exception) {
            log.error("Exception occurred while sending message to kafka, exception message: {}", exception.getMessage());
        }
    }
}
