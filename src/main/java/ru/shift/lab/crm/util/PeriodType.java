package ru.shift.lab.crm.util;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Тип периода для аналитических запросов.
 */
@Schema(description = "Тип периода: день, неделя, месяц, квартал или год")
public enum PeriodType {

    /** День. */
    DAY,

    /** Неделя (с понедельника по воскресенье). */
    WEEK,

    /** Календарный месяц. */
    MONTH,

    /** Квартал (3 месяца). */
    QUARTER,

    /** Календарный год. */
    YEAR
}