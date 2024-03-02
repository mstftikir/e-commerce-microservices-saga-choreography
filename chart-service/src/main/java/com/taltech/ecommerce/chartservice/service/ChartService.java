package com.taltech.ecommerce.chartservice.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taltech.ecommerce.chartservice.enumeration.EventStatus;
import com.taltech.ecommerce.chartservice.event.OrderEvent;
import com.taltech.ecommerce.chartservice.exception.ChartDeleteException;
import com.taltech.ecommerce.chartservice.model.Chart;
import com.taltech.ecommerce.chartservice.publisher.ChartEventPublisher;
import com.taltech.ecommerce.chartservice.repository.ChartRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ChartService {

    private final ChartRepository repository;
    private final ChartEventPublisher eventPublisher;

    public void commitDelete(OrderEvent orderEvent) {
        try {
            updateChart("Commit", orderEvent.getOrder().getUserId());
            orderEvent.getOrder().getOrderEventStatus().setChartStatus(EventStatus.SUCCESSFUL);
            eventPublisher.publishSavePayment(orderEvent);
        } catch (Exception exception) {
            log.error("Deleting chart failed with exception message: {}", exception.getMessage());
            orderEvent.getOrder().getOrderEventStatus().setChartStatus(EventStatus.FAILED);
            eventPublisher.publishRollbackInventory(orderEvent);
        }
    }

    public void rollbackDelete(OrderEvent orderEvent) {
        try {
            updateChart("Rollback", orderEvent.getOrder().getUserId());
            orderEvent.getOrder().getOrderEventStatus().setChartStatus(EventStatus.ROLLBACK);
            eventPublisher.publishRollbackInventory(orderEvent);
        } catch (Exception exception) {
            log.error("Rollbacking chart failed with exception message: {}", exception.getMessage());
            orderEvent.getOrder().getOrderEventStatus().setChartStatus(EventStatus.ROLLBACK_FAILED);
            eventPublisher.publishRollbackInventory(orderEvent);
        }
    }

    private void updateChart(String action, Long userId) {
        log.info("{} - Checking chart by userId '{}'", action, userId);
        Chart chart = repository.findByUserId(userId)
            .orElseThrow(() -> new EntityNotFoundException(String.format("%s - Chart with userId '%s' not found",
                action, userId)));

        log.info("{} - Updating chart by id '{}'", action, chart.getId());
        try {
            setChartActive(chart, action.equals("Rollback"));
            setChartUpdateDate(chart);
            repository.saveAndFlush(chart);
        }
        catch (Exception exception) {
            throw new ChartDeleteException(String.format("%s - Exception happened while updating the chart with id '%s' and userId '%s'. Exception message: '%s'",
                action,
                chart.getId(),
                chart.getUserId(),
                exception.getMessage()));
        }
    }

    private static void setChartActive(Chart chart, boolean active) {
        chart.setActive(active);
        chart.getChartItems().forEach(chartItem -> chartItem.setActive(active));
    }

    private static void setChartUpdateDate(Chart chart) {
        chart.setUpdateDate(LocalDateTime.now());
        chart.getChartItems().forEach(chartItem -> chartItem.setUpdateDate(LocalDateTime.now()));
    }
}
