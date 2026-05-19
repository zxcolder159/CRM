package ru.shift.lab.crm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.shift.lab.crm.util.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO с данными транзакции, возвращаемый клиенту.
 */
@Schema(description = "Данные транзакции")
public record TransactionDto(
        @Schema(description = "Идентификатор транзакции", example = "1")
        Long id,

        @Schema(description = "Идентификатор продавца", example = "5")
        Long sellerId,

        @Schema(description = "Сумма транзакции", example = "1500.00")
        BigDecimal amount,

        @Schema(description = "Тип оплаты")
        PaymentType paymentType,

        @Schema(description = "Дата и время транзакции", example = "2024-03-20T14:25:00")
        LocalDateTime transactionDate
) {
}