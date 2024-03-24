package com.taltech.ecommerce.paymentservice.util;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.taltech.ecommerce.paymentservice.model.Payment;
import com.taltech.ecommerce.paymentservice.model.PaymentItem;
import com.taltech.ecommerce.paymentservice.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
    private final PaymentRepository paymentRepository;
    @Override
    public void run(String... args) {
        if (paymentRepository.count() < 2) {
            //Payment 1
            PaymentItem paymentItem = new PaymentItem();
            paymentItem.setInventoryCode("iphone_13");
            paymentItem.setQuantity(new BigDecimal(1));
            paymentItem.setPrice(new BigDecimal(1000));
            paymentItem.setInsertDate(LocalDateTime.now());
            paymentItem.setUpdateDate(LocalDateTime.now());

            PaymentItem paymentItem2 = new PaymentItem();
            paymentItem2.setInventoryCode("samsung_a12");
            paymentItem2.setQuantity(new BigDecimal(1));
            paymentItem2.setPrice(new BigDecimal(800));
            paymentItem2.setInsertDate(LocalDateTime.now());
            paymentItem2.setUpdateDate(LocalDateTime.now());

            Payment payment = new Payment();
            payment.setCode(UUID.randomUUID().toString());
            payment.setUserId(12345L);
            payment.setPaymentItems(List.of(paymentItem, paymentItem2));
            payment.setTotalPrice(new BigDecimal(1800));
            payment.setActive(true);
            payment.setInsertDate(LocalDateTime.now());
            payment.setUpdateDate(LocalDateTime.now());
            paymentRepository.save(payment);

            //Payment 2
            PaymentItem paymentItem3 = new PaymentItem();
            paymentItem3.setInventoryCode("iphone_13");
            paymentItem3.setQuantity(new BigDecimal(2));
            paymentItem3.setPrice(new BigDecimal(1000));
            paymentItem3.setInsertDate(LocalDateTime.now());
            paymentItem3.setUpdateDate(LocalDateTime.now());

            PaymentItem paymentItem4 = new PaymentItem();
            paymentItem4.setInventoryCode("samsung_a12");
            paymentItem4.setQuantity(new BigDecimal(2));
            paymentItem4.setPrice(new BigDecimal(800));
            paymentItem4.setInsertDate(LocalDateTime.now());
            paymentItem4.setUpdateDate(LocalDateTime.now());

            Payment payment2 = new Payment();
            payment2.setCode(UUID.randomUUID().toString());
            payment2.setUserId(67890L);
            payment2.setPaymentItems(List.of(paymentItem3, paymentItem4));
            payment2.setTotalPrice(new BigDecimal(3600));
            payment2.setActive(true);
            payment2.setInsertDate(LocalDateTime.now());
            payment2.setUpdateDate(LocalDateTime.now());
            paymentRepository.save(payment2);
        }
    }
}
