package ru.shift.lab.crm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.shift.lab.crm.entity.Transaction;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Репозиторий для работы с сущностью Transaction (Транзакция).
 * Предоставляет методы для выполнения CRUD операций и аналитических запросов.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    /**
     * Получает все транзакции конкретного продавца.
     *
     * @param sellerId идентификатор продавца
     * @return список транзакций продавца
     */
    List<Transaction> findAllBySellerId(Long sellerId);

    /**
     * Получает идентификатор самого продуктивного продавца (с максимальной суммой) за указанный период.
     *
     * @param start начало периода в формате LocalDateTime
     * @param end   конец периода в формате LocalDateTime
     * @return список с идентификатором продавца (один элемент благодаря LIMIT 1)
     */
    @Query("SELECT t.seller.id FROM Transaction t " +
            "WHERE t.transactionDate BETWEEN :start AND :end " +
            "GROUP BY t.seller.id " +
            "ORDER BY SUM(t.amount) DESC LIMIT 1")
    List<Long> findTopSellerId(LocalDateTime start, LocalDateTime end);
}
