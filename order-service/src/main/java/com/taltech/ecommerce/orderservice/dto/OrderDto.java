package com.taltech.ecommerce.orderservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderDto {

    private Long userId;
    private List<OrderItemDto> orderItems;
    private BigDecimal totalPrice;
    private LocalDateTime insertDate;
    private LocalDateTime updateDate;
}
