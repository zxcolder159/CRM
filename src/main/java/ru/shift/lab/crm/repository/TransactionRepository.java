package ru.shift.lab.crm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.shift.lab.crm.entity.Transaction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Репозиторий для работы с транзакциями.
 * Содержит как стандартные операции поиска, так и аналитические запросы
 * для определения самого активного продавца и лучшего периода продаж.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Возвращает постраничный список транзакций конкретного продавца.
     */
    Page<Transaction> findAllBySellerId(Long sellerId, Pageable pageable);

    /**
     * Находит идентификатор продавца с наибольшей суммой транзакций за период.
     * Учитываются только активные (не удалённые) продавцы.
     * Если транзакций за период нет — возвращает пустой {@code Optional}.
     *
     * @param start начало периода (включительно)
     * @param end   конец периода (включительно)
     */
    @Query("SELECT t.seller.id FROM Transaction t " +
            "JOIN t.seller s " +
            "WHERE t.transactionDate BETWEEN :start AND :end " +
            "AND s.isDeleted = false " +
            "GROUP BY t.seller.id " +
            "ORDER BY SUM(t.amount) DESC LIMIT 1")
    Optional<Long> findTopSellerId(LocalDateTime start, LocalDateTime end);

    /**
     * Находит день с наибольшим количеством транзакций у указанного продавца.
     * Возвращает дату начала этого дня.
     */
    @Query("SELECT CAST(t.transactionDate AS DATE) FROM Transaction t " +
            "WHERE t.seller.id = :sellerId " +
            "GROUP BY CAST(t.transactionDate AS DATE) " +
            "ORDER BY COUNT(t.id) DESC LIMIT 1")
    Optional<LocalDate> findBestDay(Long sellerId);

    /**
     * Находит неделю с наибольшим количеством транзакций у указанного продавца.
     * Возвращает дату понедельника этой недели (через {@code DATE_TRUNC('week', ...)}).
     */
    @Query("SELECT CAST(FUNCTION('DATE_TRUNC', 'week', t.transactionDate) AS DATE) FROM Transaction t " +
            "WHERE t.seller.id = :sellerId " +
            "GROUP BY CAST(FUNCTION('DATE_TRUNC', 'week', t.transactionDate) AS DATE) " +
            "ORDER BY COUNT(t.id) DESC LIMIT 1")
    Optional<LocalDate> findBestWeek(Long sellerId);

    /**
     * Находит месяц с наибольшим количеством транзакций у указанного продавца.
     * Возвращает дату первого числа этого месяца.
     */
    @Query("SELECT CAST(FUNCTION('DATE_TRUNC', 'month', t.transactionDate) AS DATE) FROM Transaction t " +
            "WHERE t.seller.id = :sellerId " +
            "GROUP BY CAST(FUNCTION('DATE_TRUNC', 'month', t.transactionDate) AS DATE) " +
            "ORDER BY COUNT(t.id) DESC LIMIT 1")
    Optional<LocalDate> findBestMonth(Long sellerId);

    /**
     * Находит квартал с наибольшим количеством транзакций у указанного продавца.
     * Возвращает дату первого числа первого месяца этого квартала.
     */
    @Query("SELECT CAST(FUNCTION('DATE_TRUNC', 'quarter', t.transactionDate) AS DATE) FROM Transaction t " +
            "WHERE t.seller.id = :sellerId " +
            "GROUP BY CAST(FUNCTION('DATE_TRUNC', 'quarter', t.transactionDate) AS DATE) " +
            "ORDER BY COUNT(t.id) DESC LIMIT 1")
    Optional<LocalDate> findBestQuarter(Long sellerId);

    /**
     * Находит год с наибольшим количеством транзакций у указанного продавца.
     * Возвращает дату 1 января этого года.
     */
    @Query("SELECT CAST(FUNCTION('DATE_TRUNC', 'year', t.transactionDate) AS DATE) FROM Transaction t " +
            "WHERE t.seller.id = :sellerId " +
            "GROUP BY CAST(FUNCTION('DATE_TRUNC', 'year', t.transactionDate) AS DATE) " +
            "ORDER BY COUNT(t.id) DESC LIMIT 1")
    Optional<LocalDate> findBestYear(Long sellerId);
}