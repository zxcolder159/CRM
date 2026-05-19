package ru.shift.lab.crm.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO с описанием ошибки, возвращаемый при исключениях.
 */
@Schema(description = "Описание ошибки")
public record ErrorResponseDto(
        @Schema(description = "Время возникновения ошибки", example = "2024-03-20T14:25:00")
        LocalDateTime timeStamp,

        @Schema(description = "Сообщение об ошибке", example = "Продавец с id 99 не найден")
        String message,

        @Schema(description = "HTTP-код ответа", example = "404")
        int status
) {
}