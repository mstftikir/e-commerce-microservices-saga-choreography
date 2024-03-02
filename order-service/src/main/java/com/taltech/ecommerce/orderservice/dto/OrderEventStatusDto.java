package com.taltech.ecommerce.orderservice.dto;

import com.taltech.ecommerce.orderservice.enumeration.EventStatus;

import lombok.Data;

@Data
public class OrderEventStatusDto {
    private String id;
    private EventStatus inventoryStatus;
    private EventStatus chartStatus;
    private EventStatus paymentStatus;
}
