package com.taltech.ecommerce.chartservice.event;

import com.taltech.ecommerce.chartservice.dto.OrderDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEvent {

    private OrderDto order;
}
