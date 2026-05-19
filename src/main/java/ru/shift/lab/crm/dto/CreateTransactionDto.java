package ru.shift.lab.crm.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ru.shift.lab.crm.util.PaymentType;

import java.math.BigDecimal;

/** DTO для создания транзакции. */
public record CreateTransactionDto(
        @NotNull(message = "ID продавца не может быть пустым")
        Long sellerId,

        @NotNull(message = "Сумма не может быть пустой")
        @Positive(message = "Сумма должна быть больше 0")
        BigDecimal amount,

        @NotNull(message = "Тип платежа не может быть пустым")
        PaymentType paymentType
) {
}
