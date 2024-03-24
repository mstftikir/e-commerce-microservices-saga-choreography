package com.taltech.ecommerce.paymentservice.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taltech.ecommerce.paymentservice.dto.OrderDto;
import com.taltech.ecommerce.paymentservice.enumeration.EventStatus;
import com.taltech.ecommerce.paymentservice.event.OrderEvent;
import com.taltech.ecommerce.paymentservice.exception.PaymentSaveException;
import com.taltech.ecommerce.paymentservice.model.Payment;
import com.taltech.ecommerce.paymentservice.model.PaymentItem;
import com.taltech.ecommerce.paymentservice.publisher.PaymentEventPublisher;
import com.taltech.ecommerce.paymentservice.repository.PaymentRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PaymentService {

    private static final List<Long> NOT_ALLOWED_USERS = List.of(99999L);

    private final PaymentRepository repository;
    private final PaymentEventPublisher eventPublisher;

    public void commitSave(OrderEvent orderEvent) {
        Payment payment = getPayment(orderEvent);
        try {
            Payment savedPayment = savePayment("Commit", payment);
            orderEvent.getOrder().setPaymentCode(savedPayment.getCode());
            orderEvent.getOrder().setTotalPrice(savedPayment.getTotalPrice());
            orderEvent.getOrder().getOrderEventStatus().setPaymentStatus(EventStatus.SUCCESSFUL);
            eventPublisher.publishOrderCompleted(orderEvent);
        } catch (Exception exception) {
            log.error("Saving payment failed with exception message: {}", exception.getMessage());
            orderEvent.getOrder().getOrderEventStatus().setPaymentStatus(EventStatus.FAILED);
            eventPublisher.publishRollbackChart(orderEvent);
        }
    }

    public void rollbackSave(OrderEvent orderEvent) {
        Payment payment = getPayment(orderEvent);
        try {
            Payment savedPayment = savePayment("Rollback", payment);
            orderEvent.getOrder().setPaymentCode(savedPayment.getCode());
            orderEvent.getOrder().setTotalPrice(savedPayment.getTotalPrice());
            orderEvent.getOrder().getOrderEventStatus().setPaymentStatus(EventStatus.ROLLBACK);
            eventPublisher.publishRollbackChart(orderEvent);

        } catch (Exception exception) {
            log.error("Rollbacking payment failed with exception message: {}", exception.getMessage());
            orderEvent.getOrder().getOrderEventStatus().setPaymentStatus(EventStatus.ROLLBACK_FAILED);
            eventPublisher.publishRollbackChart(orderEvent);
        }
    }

    private Payment getPayment(OrderEvent orderEvent) {
        OrderDto order = orderEvent.getOrder();
        String paymentCode = order.getPaymentCode() == null
            ? UUID.randomUUID().toString()
            : order.getPaymentCode();
        List<PaymentItem> paymentItems = new ArrayList<>();
        order.getOrderItems().forEach(orderItem -> paymentItems.add(PaymentItem.builder()
            .inventoryCode(orderItem.getInventoryCode())
            .quantity(orderItem.getQuantity())
            .price(orderItem.getPrice())
            .build()));
        return Payment.builder()
            .code(paymentCode)
            .userId(order.getUserId())
            .paymentItems(paymentItems)
            .build();
    }

    private Payment savePayment(String action, Payment payment) {
        validate(payment);

        log.info("{} - Saving the payment for userId '{}' and code '{}'",
            action,
            payment.getUserId(),
            payment.getCode());

        boolean rollback = action.equals("Rollback");
        if(rollback) {
            String paymentCode = payment.getCode();
            payment = repository.findByCode(payment.getCode()).orElseThrow(() ->
                new EntityNotFoundException(String.format("%s - Payment with code '%s' not found",
                    action,
                    paymentCode)));
        }
        else{
            calculateTotalPrice(payment);
        }

        setPaymentActive(payment, !rollback);
        if(action.equals("Commit")) {
            setPaymentInsertDates(payment);
        }
        setPaymentUpdateDates(payment);

        try {
            return repository.saveAndFlush(payment);
        }
        catch (Exception exception) {
            throw new PaymentSaveException(String.format("%s - Payment save failed for userId '%s' and code '%s'",
                action,
                payment.getUserId(),
                payment.getCode()));
        }
    }

    private static void validate(Payment payment) {
        NOT_ALLOWED_USERS.forEach(notAllowedUser -> {
            if(notAllowedUser.equals(payment.getUserId())) {
                throw new PaymentSaveException(String.format("Payments are not allowed for user '%s'", payment.getUserId()));
            }
        });
    }

    private void setPaymentActive(Payment payment, boolean active) {
        payment.setActive(active);
        payment.getPaymentItems().forEach(paymentItem -> paymentItem.setActive(true));
    }

    private void calculateTotalPrice(Payment payment) {
        AtomicReference<BigDecimal> totalPrice = new AtomicReference<>(BigDecimal.ZERO);
        payment.getPaymentItems().forEach(paymentItem -> totalPrice.updateAndGet(t -> t.add(paymentItem.getPrice().multiply(paymentItem.getQuantity()))));
        payment.setTotalPrice(totalPrice.get());
    }

    private void setPaymentInsertDates(Payment payment) {
        payment.setInsertDate(LocalDateTime.now());
        payment.getPaymentItems().forEach(paymentItem -> paymentItem.setInsertDate(LocalDateTime.now()));
    }

    private void setPaymentUpdateDates(Payment payment) {
        payment.setUpdateDate(LocalDateTime.now());
        payment.getPaymentItems().forEach(paymentItem -> paymentItem.setUpdateDate(LocalDateTime.now()));
    }
}
