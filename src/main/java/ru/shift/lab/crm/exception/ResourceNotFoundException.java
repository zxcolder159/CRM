package ru.shift.lab.crm.exception;

/** Исключение при не найденном ресурсе в БД. */
public class ResourceNotFoundException extends RuntimeException {
    /** Конструктор с сообщением об ошибке. */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
