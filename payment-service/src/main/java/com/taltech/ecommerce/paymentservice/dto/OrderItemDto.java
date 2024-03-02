package com.taltech.ecommerce.paymentservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class OrderItemDto {
    private String inventoryCode;
    private Integer quantity;
    private BigDecimal price;
    private LocalDateTime insertDate;
    private LocalDateTime updateDate;
}
