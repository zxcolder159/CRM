package ru.shift.lab.crm.dto;

import java.time.LocalDateTime;

public record ErrorResponseDto (
        LocalDateTime timeStamp,
        String message,
        int status
) {
}
