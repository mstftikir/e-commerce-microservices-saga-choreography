package com.taltech.ecommerce.inventoryservice.util;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.taltech.ecommerce.inventoryservice.model.Inventory;
import com.taltech.ecommerce.inventoryservice.repository.InventoryRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
    private final InventoryRepository inventoryRepository;
    @Override
    public void run(String... args) {
        if (inventoryRepository.count() < 2) {
            Inventory inventory = new Inventory();
            inventory.setCode("iphone_13");
            inventory.setName("Iphone 13");
            inventory.setDescription("Iphone 13 series");
            inventory.setQuantity(new BigDecimal(100000000));
            inventory.setPrice(new BigDecimal(1000));
            inventory.setInsertDate(LocalDateTime.now());
            inventory.setUpdateDate(LocalDateTime.now());

            Inventory inventory1 = new Inventory();
            inventory1.setCode("samsung_a12");
            inventory1.setName("Samsung A12");
            inventory1.setDescription("Samsung A series");
            inventory1.setQuantity(new BigDecimal(100000000));
            inventory1.setPrice(new BigDecimal(800));
            inventory1.setInsertDate(LocalDateTime.now());
            inventory1.setUpdateDate(LocalDateTime.now());

            inventoryRepository.save(inventory);
            inventoryRepository.save(inventory1);
        }
    }
}
