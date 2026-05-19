package ru.shift.lab.crm.dto;

import ru.shift.lab.crm.util.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** DTO для Транзакции. */
public record TransactionDto(
        Long id,
        Long sellerId,
        BigDecimal amount,
        PaymentType paymentType,
        LocalDateTime transactionDate
) {
}
