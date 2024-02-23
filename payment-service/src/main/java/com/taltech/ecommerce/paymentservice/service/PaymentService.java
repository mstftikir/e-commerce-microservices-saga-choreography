package com.taltech.ecommerce.paymentservice.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

import org.hibernate.exception.GenericJDBCException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taltech.ecommerce.paymentservice.exception.PaymentSaveException;
import com.taltech.ecommerce.paymentservice.model.Payment;
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

    private final PaymentRepository repository;
    private final PaymentEventPublisher eventPublisher;

    public void commitSave(Payment payment) {
        try {
            Payment savedPayment = savePayment("Commit", payment);
            eventPublisher.publishPaymentSaved(savedPayment);

        } catch (Exception exception) {
            log.error("Saving payment failed with exception message: {}", exception.getMessage());
            eventPublisher.publishPaymentSaveFailed(payment);
        }
    }

    public void rollbackSave(Payment payment) {
        try {
            Payment savedPayment = savePayment("Rollback", payment);
            eventPublisher.publishPaymentRollbacked(savedPayment);

        } catch (Exception exception) {
            log.error("Rollbacking payment failed with exception message: {}", exception.getMessage());
            eventPublisher.publishPaymentRollbackFailed(payment);
        }
    }

    private Payment savePayment(String action, Payment payment) {
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
            return repository.save(payment);
        }
        catch (Exception exception) {
            GenericJDBCException jdbcException = (GenericJDBCException) exception.getCause();
            // Insert not allowed in readonly transaction
            if(jdbcException.getSQLState().equals("25006")) {
                return payment;
            }

            throw new PaymentSaveException(String.format("%s - Payment save failed for userId '%s' and code '%s'",
                action,
                payment.getUserId(),
                payment.getCode()));
        }
    }

    private void setPaymentActive(Payment payment, boolean active) {
        payment.setActive(active);
        payment.getPaymentItems().forEach(paymentItem -> paymentItem.setActive(true));
    }

    private void calculateTotalPrice(Payment payment) {
        AtomicReference<BigDecimal> totalPrice = new AtomicReference<>(BigDecimal.ZERO);
        payment.getPaymentItems().forEach(paymentItem -> totalPrice.updateAndGet(t -> t.add(paymentItem.getPrice().multiply(new BigDecimal(paymentItem.getQuantity())))));
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
