package ru.shift.lab.crm.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Сущность для БД продавец.
 * Представляет продавца в системе с его личной информацией и датой регистрации.
 */
@Entity
@NoArgsConstructor
@Getter
@Setter
public class Seller {
    /**
     * Уникальный идентификатор продавца (автоинкремент).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    /**
     * Имя продавца.
     */
    private String name;

    /**
     * Контактная информация продавца (телефон, email и т.д.).
     */
    private String contactInfo;

    /**
     * Дата и время регистрации продавца в системе.
     */
    private LocalDateTime registrationDate;
}
