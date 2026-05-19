package ru.shift.lab.crm.dto;

import jakarta.validation.constraints.NotBlank;

/** DTO для создания продавца. */
public record CreateSellerDto(
        @NotBlank(message = "Имя продавца не может быть пустым")
        String name,

        @NotBlank(message = "Контактная информация не может быть пустой")
        String contactInfo
) {
}

