package com.taltech.ecommerce.chartservice.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.taltech.ecommerce.chartservice.event.OrderEvent;
import com.taltech.ecommerce.chartservice.service.ChartService;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final ChartService service;
    private final ObservationRegistry observationRegistry;

    @KafkaListener(topics = "deleteChartTopic")
    public void receiveDeleteChart(OrderEvent orderEvent) {
        Observation.createNotStarted("delete-chart-received", this.observationRegistry)
            .observe(() -> {
                log.info("Delete chart event received");
                service.commitDelete(orderEvent);
            });
    }

    @KafkaListener(topics = "rollbackChartTopic")
    public void receiveRollbackChart(OrderEvent orderEvent) {
        Observation.createNotStarted("rollback-chart-received", this.observationRegistry)
            .observe(() -> {
                log.info("Rollback chart event received");
                service.rollbackDelete(orderEvent);
            });
    }
}
