package com.taltech.ecommerce.paymentservice.dto;

import com.taltech.ecommerce.paymentservice.enumeration.EventStatus;

import lombok.Data;

@Data
public class OrderEventStatusDto {
    private String id;
    private EventStatus inventoryStatus;
    private EventStatus chartStatus;
    private EventStatus paymentStatus;
}
