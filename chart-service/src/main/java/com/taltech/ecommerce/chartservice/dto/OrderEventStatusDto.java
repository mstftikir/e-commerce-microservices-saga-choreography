package com.taltech.ecommerce.chartservice.dto;

import com.taltech.ecommerce.chartservice.enumeration.EventStatus;

import lombok.Data;

@Data
public class OrderEventStatusDto {
    private String id;
    private EventStatus inventoryStatus;
    private EventStatus chartStatus;
    private EventStatus paymentStatus;
}
