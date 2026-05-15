package ru.shift.lab.crm.dto;

/** DTO для обновления продавца. */
public record UpdateSellerDto(
        String name,
        String contactInfo
) {
}

