package ru.shift.lab.crm.util;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Тип оплаты транзакции.
 */
@Schema(description = "Тип оплаты: наличные, карта или перевод")
public enum PaymentType {

    /** Наличные. */
    CASH,

    /** Банковская карта. */
    CARD,

    /** Банковский перевод. */
    TRANSFER
}