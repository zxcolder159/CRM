package ru.shift.lab.crm.controller.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.shift.lab.crm.dto.ErrorResponseDto;
import ru.shift.lab.crm.exception.ResourceNotFoundException;

import jakarta.validation.ConstraintViolationException;

/**
 * Глобальный обработчик исключений.
 * <p>
 * Перехватывает все исключения, брошенные из контроллеров, и возвращает
 * единообразный JSON-ответ в формате {@link ErrorResponseDto}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обрабатывает ситуацию, когда запрошенный ресурс не найден в базе данных.
     * Возвращает HTTP 404 с описанием того, чего именно не нашлось.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /**
     * Обрабатывает ошибку несоответствия типа параметра запроса.
     * Например, если в качестве числового id передана строка или неизвестное значение enum.
     * Возвращает HTTP 400 с указанием имени параметра и ожидаемого типа.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String typeName = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "неизвестный";
        String message = "Некорректный параметр '" + ex.getName() + "': ожидался тип " + typeName;
        return buildResponse(message, HttpStatus.BAD_REQUEST);
    }

    /**
     * Обрабатывает ошибку чтения тела запроса.
     * Возникает, когда JSON невалиден или структура не соответствует ожидаемой.
     * Возвращает HTTP 400.
     */
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleHttpMessageNotReadable() {
        return buildResponse("Некорректный формат тела запроса", HttpStatus.BAD_REQUEST);
    }

    /**
     * Обрабатывает отсутствие обязательного параметра запроса.
     * Возвращает HTTP 400 с именем пропущенного параметра.
     */
    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponseDto> handleMissingServletRequestParameter(
            org.springframework.web.bind.MissingServletRequestParameterException ex) {
        return buildResponse("Отсутствует обязательный параметр: " + ex.getParameterName(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Обрабатывает нарушение ограничений Bean Validation на уровне параметров метода.
     * Возвращает HTTP 400 с сообщением из аннотации валидации.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleConstraintViolation(ConstraintViolationException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Обрабатывает ошибки валидации полей тела запроса, помеченного {@code @Valid}.
     * Возвращает HTTP 400 с сообщением первой найденной ошибки поля.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldError() != null
                ? ex.getBindingResult().getFieldError().getDefaultMessage()
                : "Ошибка валидации";
        return buildResponse(message, HttpStatus.BAD_REQUEST);
    }

    /**
     * Обрабатывает нарушение целостности данных в базе данных.
     * Возникает, например, при попытке вставить дублирующийся уникальный ключ.
     * Возвращает HTTP 409.
     */
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleDataIntegrityViolation() {
        return buildResponse("Нарушение целостности данных", HttpStatus.CONFLICT);
    }

    /**
     * Запасной обработчик для всех непредвиденных исключений.
     * Возвращает HTTP 500 с сообщением об ошибке.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception ex) {
        return buildResponse("Произошла непредвиденная ошибка", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Собирает тело ответа с текущим временем, переданным сообщением и HTTP-статусом.
     */
    private ResponseEntity<ErrorResponseDto> buildResponse(String message, HttpStatus status) {
        ErrorResponseDto body = new ErrorResponseDto(java.time.LocalDateTime.now(), message, status.value());
        return ResponseEntity.status(status).body(body);
    }
}
