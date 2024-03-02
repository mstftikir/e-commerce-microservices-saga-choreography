package com.taltech.ecommerce.orderservice.model;

import com.taltech.ecommerce.orderservice.enumeration.EventStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderEventStatus {
    @Id
    private String id;
    @Version
    private Long version;
    @Enumerated(EnumType.ORDINAL)
    private EventStatus inventoryStatus;
    @Enumerated(EnumType.ORDINAL)
    private EventStatus chartStatus;
    @Enumerated(EnumType.ORDINAL)
    private EventStatus paymentStatus;
}
