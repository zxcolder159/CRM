package ru.shift.lab.crm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.shift.lab.crm.util.PeriodType;

import java.time.LocalDate;

/**
 * DTO с результатом поиска лучшего периода продаж продавца.
 */
@Schema(description = "Лучший период продаж продавца")
public record BestPeriodResultDto(
        @Schema(description = "Идентификатор продавца", example = "3")
        Long sellerId,

        @Schema(description = "Начало лучшего периода", example = "2024-01-01")
        LocalDate periodStart,

        @Schema(description = "Конец лучшего периода", example = "2024-03-31")
        LocalDate periodEnd,

        @Schema(description = "Тип периода")
        PeriodType periodType
) {
}