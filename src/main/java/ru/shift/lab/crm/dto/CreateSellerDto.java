package ru.shift.lab.crm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO для создания нового продавца.
 */
@Schema(description = "Данные для создания продавца")
public record CreateSellerDto(
        @Schema(description = "Имя продавца", example = "Иван Иванов")
        @NotBlank(message = "Имя продавца не может быть пустым")
        String name,

        @Schema(description = "Контактная информация", example = "ivan@example.com")
        @NotBlank(message = "Контактная информация не может быть пустой")
        String contactInfo
) {
}