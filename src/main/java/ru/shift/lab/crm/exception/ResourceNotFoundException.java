package ru.shift.lab.crm.exception;

/**
 * Исключение выбрасывается когда запрашиваемый ресурс не найден в базе данных.
 */
public class ResourceNotFoundException extends RuntimeException {
    /**
     * Конструктор с сообщением об ошибке.
     *
     * @param message описание ошибки
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
