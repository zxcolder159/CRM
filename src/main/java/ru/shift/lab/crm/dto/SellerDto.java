package ru.shift.lab.crm.dto;

import java.time.LocalDateTime;

/** DTO для Продавца. */
public record SellerDto(
        Long id,
        String name,
        String contactInfo,
        LocalDateTime registrationDate
) {
}

