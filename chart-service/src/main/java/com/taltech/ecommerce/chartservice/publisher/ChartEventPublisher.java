package com.taltech.ecommerce.chartservice.publisher;

import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import com.taltech.ecommerce.chartservice.event.OrderEvent;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChartEventPublisher {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private final ObservationRegistry observationRegistry;

    public void publishSavePayment(OrderEvent orderEvent) {
        publishEvent("savePaymentTopic", "save-payment-sent", orderEvent);
    }

    public void publishChartDeleteFailed(OrderEvent orderEvent) {
        publishEvent("chartDeleteFailedTopic", "chart-delete-failed-sent", orderEvent);
    }

    public void publishChartRollbacked(OrderEvent orderEvent) {
        publishEvent("chartRollbackedTopic", "chart-rollbacked-sent", orderEvent);
    }

    public void publishChartRollbackFailed(OrderEvent orderEvent) {
        publishEvent("chartRollbackFailedTopic", "chart-rollback-failed-sent", orderEvent);
    }

    private void publishEvent(String topic, String observationName, OrderEvent orderEvent) {
        log.info("Publishing chart event '{}' to '{}'", topic, orderEvent.getOrder().getOrderEventStatus().getId());

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
