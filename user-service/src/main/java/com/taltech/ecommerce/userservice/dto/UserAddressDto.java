package com.taltech.ecommerce.userservice.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserAddressDto {

    private String name;
    private String country;
    private String city;
    private String district;
    private String address;
    private LocalDateTime insertDate;
    private LocalDateTime updateDate;
}
