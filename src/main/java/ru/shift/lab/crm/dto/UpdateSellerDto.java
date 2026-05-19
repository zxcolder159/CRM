package ru.shift.lab.crm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO для обновления данных продавца.
 */
@Schema(description = "Данные для обновления продавца")
public record UpdateSellerDto(
        @Schema(description = "Новое имя продавца", example = "Пётр Петров")
        @NotBlank(message = "Имя продавца не может быть пустым")
        String name,

        @Schema(description = "Новая контактная информация", example = "petr@example.com")
        @NotBlank(message = "Контактная информация не может быть пустой")
        String contactInfo
) {
}