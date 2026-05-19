package ru.shift.lab.crm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ru.shift.lab.crm.util.PaymentType;

import java.math.BigDecimal;

/**
 * DTO для создания новой транзакции.
 */
@Schema(description = "Данные для создания транзакции")
public record CreateTransactionDto(
        @Schema(description = "Идентификатор продавца", example = "5")
        @NotNull(message = "ID продавца не может быть пустым")
        Long sellerId,

        @Schema(description = "Сумма транзакции, должна быть больше нуля", example = "1500.00")
        @NotNull(message = "Сумма не может быть пустой")
        @Positive(message = "Сумма должна быть больше 0")
        BigDecimal amount,

        @Schema(description = "Тип оплаты")
        @NotNull(message = "Тип платежа не может быть пустым")
        PaymentType paymentType
) {
}