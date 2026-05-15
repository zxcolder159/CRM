package ru.shift.lab.crm.dto;

import ru.shift.lab.crm.util.PeriodType;

import java.time.LocalDate;

/** Результат поиска лучшего периода продаж. */
public record BestPeriodResultDto(
        Long sellerId,
        LocalDate periodStart,
        PeriodType periodType
) {
}

