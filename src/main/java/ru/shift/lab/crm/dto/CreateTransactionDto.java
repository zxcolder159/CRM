package ru.shift.lab.crm.dto;

import java.math.BigDecimal;

/** DTO для создания транзакции. */
public record CreateTransactionDto(
        Long sellerId,
        BigDecimal amount,
        String paymentType
) {
}

