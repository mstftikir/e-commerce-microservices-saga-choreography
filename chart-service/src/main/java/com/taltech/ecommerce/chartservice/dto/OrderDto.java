package com.taltech.ecommerce.chartservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class OrderDto {
    private OrderEventStatusDto orderEventStatus;
    private Long userId;
    private List<OrderItemDto> orderItems;
    private String paymentCode;
    private BigDecimal totalPrice;
    private LocalDateTime insertDate;
    private LocalDateTime updateDate;
}
