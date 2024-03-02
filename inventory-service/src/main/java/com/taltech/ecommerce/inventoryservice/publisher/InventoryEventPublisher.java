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
public class InventoryEventPublisher {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private final ObservationRegistry observationRegistry;

    public void publishDeleteChart(OrderEvent orderEvent) {
        publishEvent("deleteChartTopic", "delete-chart-sent", orderEvent);
    }

    public void publishOrderFailed(OrderEvent orderEvent) {
        publishEvent("orderFailedTopic", "order-failed-sent", orderEvent);
    }

    private void publishEvent(String topic, String observationName, OrderEvent orderEvent) {
        log.info("Publishing inventory event '{}' to '{}'", topic, orderEvent.getOrder().getOrderEventStatus().getId());

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

