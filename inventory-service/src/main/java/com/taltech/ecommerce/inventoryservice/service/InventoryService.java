package com.taltech.ecommerce.inventoryservice.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taltech.ecommerce.inventoryservice.enumeration.EventStatus;
import com.taltech.ecommerce.inventoryservice.event.OrderEvent;
import com.taltech.ecommerce.inventoryservice.exception.InventoryLimitException;
import com.taltech.ecommerce.inventoryservice.model.Inventory;
import com.taltech.ecommerce.inventoryservice.publisher.InventoryEventPublisher;
import com.taltech.ecommerce.inventoryservice.repository.InventoryRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class InventoryService {

    private final InventoryRepository repository;
    private final InventoryEventPublisher eventPublisher;

    public void commitUpdate(OrderEvent orderEvent) {
        List<Inventory> inventoryList = getInventories(orderEvent);
        try {
            updateInventories("Commit", inventoryList);
            orderEvent.getOrder().getOrderEventStatus().setInventoryStatus(EventStatus.SUCCESSFUL);
            eventPublisher.publishDeleteChart(orderEvent);
        }
        catch (Exception exception) {
            log.error("Updating inventory failed with exception message: {}", exception.getMessage());
            orderEvent.getOrder().getOrderEventStatus().setInventoryStatus(EventStatus.FAILED);
            eventPublisher.publishOrderFailed(orderEvent);
        }
    }

    public void rollbackUpdate(OrderEvent orderEvent) {
        List<Inventory> inventoryList = getInventories(orderEvent);
        try {
            updateInventories("Rollback", inventoryList);
            orderEvent.getOrder().getOrderEventStatus().setInventoryStatus(EventStatus.ROLLBACK);
            eventPublisher.publishOrderFailed(orderEvent);
        }
        catch (Exception exception) {
            log.error("Rollbacking inventory failed with exception message: {}", exception.getMessage());
            orderEvent.getOrder().getOrderEventStatus().setInventoryStatus(EventStatus.ROLLBACK_FAILED);
            eventPublisher.publishOrderFailed(orderEvent);
        }
    }

    private List<Inventory> getInventories(OrderEvent orderEvent) {
        List<Inventory> inventoryList = new ArrayList<>();
        orderEvent.getOrder().getOrderItems()
            .forEach(orderItem -> inventoryList.add(Inventory.builder()
                .code(orderItem.getInventoryCode())
                .quantity(orderItem.getQuantity())
                .build())
            );
        return inventoryList;
    }

    private void updateInventories(String action, List<Inventory> inventoryList) {
        List<Inventory> foundInventoryList = findByCode(action, inventoryList);

        isInStock(action, foundInventoryList);

        foundInventoryList.forEach(foundInventory -> {
            Integer receivedQuantity = inventoryList.stream().filter(inventory -> inventory.getCode().equals(foundInventory.getCode()))
                .map(Inventory::getQuantity)
                .findAny()
                .orElseThrow(EntityNotFoundException::new);
            Integer foundQuantity = foundInventory.getQuantity();
            int calculatedQuantity;

            if(action.equals("Rollback")){
                calculatedQuantity = foundQuantity + receivedQuantity;
            }
            else {
                calculatedQuantity = foundQuantity - receivedQuantity;
                if (calculatedQuantity < 0) {
                    throw new InventoryLimitException(String.format("%s - Received inventory quantity '%s' is more than found inventory quantity '%s'",
                        action,
                        receivedQuantity,
                        foundQuantity));
                }
            }

            foundInventory.setQuantity(calculatedQuantity);
            foundInventory.setUpdateDate(LocalDateTime.now());
        });

        repository.saveAllAndFlush(foundInventoryList);
    }

    private List<Inventory> findByCode(String action, List<Inventory> receivedInventoryList) {
        log.info("{} - Checking inventory for '{}' inventory items", action, receivedInventoryList.size());
        List<String> codes = receivedInventoryList.stream().map(Inventory::getCode).toList();
        List<Inventory> foundInventoryList = repository.findByCodeIn(codes);
        if(foundInventoryList.size() != receivedInventoryList.size()) {
            throw new EntityNotFoundException(String.format("%s - Received inventory size '%s' and found inventory size '%s' is not equal",
                action,
                receivedInventoryList.size(),
                foundInventoryList.size()));
        }
        return foundInventoryList;
    }

    private void isInStock(String action, List<Inventory> inventoryList) {
        inventoryList.forEach(inventory -> {
            if(inventory.getQuantity() != null && inventory.getQuantity() <= 0) {
                throw new EntityNotFoundException(String.format("%s - Not enough quantity '%s' for inventory item code '%s'",
                    action,
                    inventory.getQuantity(),
                    inventory.getCode()));
            }
        });
    }
}
