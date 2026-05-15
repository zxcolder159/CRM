package ru.shift.lab.crm.dto;

/** DTO для создания продавца. */
public record CreateSellerDto(
        String name,
        String contactInfo
) {
}

