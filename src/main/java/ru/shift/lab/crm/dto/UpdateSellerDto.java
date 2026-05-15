package ru.shift.lab.crm.dto;

import jakarta.validation.constraints.NotBlank;

/** DTO для обновления продавца. */
public record UpdateSellerDto(
        @NotBlank(message = "Имя продавца не может быть пустым")
        String name,

        @NotBlank(message = "Контактная информация не может быть пустой")
        String contactInfo
) {
}

