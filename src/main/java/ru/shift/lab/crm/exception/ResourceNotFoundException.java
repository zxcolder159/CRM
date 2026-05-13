package ru.shift.lab.crm.exception;

/** Исключение при не найденном ресурсе в БД. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
