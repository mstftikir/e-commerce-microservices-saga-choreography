package com.taltech.ecommerce.inventoryservice.dto;

import com.taltech.ecommerce.inventoryservice.enumeration.EventStatus;

import lombok.Data;

@Data
public class OrderEventStatusDto {
    private String id;
    private EventStatus inventoryStatus;
    private EventStatus chartStatus;
    private EventStatus paymentStatus;
}
