package ru.shift.lab.crm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/** DTO для создания транзакции. */
public record CreateTransactionDto(
        @NotNull(message = "ID продавца не может быть пустым")
        Long sellerId,
        
        @NotNull(message = "Сумма не может быть пустой")
        @Positive(message = "Сумма должна быть больше 0")
        BigDecimal amount,
        
        @NotBlank(message = "Тип платежа не может быть пустым")
        String paymentType
) {
}

