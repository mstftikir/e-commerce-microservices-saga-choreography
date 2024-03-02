package com.taltech.ecommerce.paymentservice.publisher;

import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import com.taltech.ecommerce.paymentservice.event.OrderEvent;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisher {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private final ObservationRegistry observationRegistry;

    public void publishOrderCompleted(OrderEvent orderEvent) {
        publishEvent("orderCompletedTopic", "payment-saved-sent", orderEvent);
    }

    public void publishPaymentSaveFailed(OrderEvent orderEvent) {
        publishEvent("paymentSaveFailedTopic", "payment-save-failed-sent", orderEvent);
    }

    public void publishPaymentRollbacked(OrderEvent orderEvent) {
        publishEvent("paymentRollbackedTopic", "payment-rollbacked-sent", orderEvent);
    }

    public void publishPaymentRollbackFailed(OrderEvent orderEvent) {
        publishEvent("paymentRollbackFailedTopic", "payment-rollback-failed-sent", orderEvent);
    }

    private void publishEvent(String topic, String observationName, OrderEvent orderEvent) {
        log.info("Publishing payment event to '{}'", topic);

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

