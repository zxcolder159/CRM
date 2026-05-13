package ru.shift.lab.crm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Сущность для БД транзакция.
 * Представляет транзакцию (платеж) продавца с информацией о сумме, типе оплаты и дате.
 */
@Entity
@NoArgsConstructor
@Getter
@Setter
public class Transaction {
    /**
     * Уникальный идентификатор транзакции (автоинкремент).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    /**
     * Ссылка на продавца (внешний ключ).
     * Используется LAZY загрузка для оптимизации.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Seller seller;

    /**
     * Сумма транзакции в денежных единицах (используется BigDecimal для точности).
     */
    private BigDecimal amount;

    /**
     * Тип оплаты: CASH (наличные), CARD (карта), TRANSFER (переводКакого).
     */
    private String paymentType;

    /**
     * Дата и время проведения транзакции.
     */
    private LocalDateTime transactionDate;
}
