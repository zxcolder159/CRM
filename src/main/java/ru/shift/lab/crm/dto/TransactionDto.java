package ru.shift.lab.crm.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** DTO для Транзакции. */
public record TransactionDto(
        Long id,
        Long sellerId,
        BigDecimal amount,
        String paymentType,
        LocalDateTime transactionDate
) {
}

