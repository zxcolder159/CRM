package ru.shift.lab.crm.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO с данными продавца, возвращаемый клиенту.
 */
@Schema(description = "Данные продавца")
public record SellerDto(
        @Schema(description = "Идентификатор продавца", example = "1")
        Long id,

        @Schema(description = "Имя продавца", example = "Иван Иванов")
        String name,

        @Schema(description = "Контактная информация", example = "ivan@example.com")
        String contactInfo,

        @Schema(description = "Дата и время регистрации", example = "2024-01-15T10:30:00")
        LocalDateTime registrationDate
) {
}