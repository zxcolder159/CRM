package ru.shift.lab.crm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.shift.lab.crm.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Репозиторий Транзакции. */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    /** Получает все транзакции продавца по его id. */
    List<Transaction> findAllBySellerId(Long sellerId);

    /** Получает id самого продуктивного продавца за период. */
    @Query("SELECT t.seller.id FROM Transaction t " +
            "WHERE t.transactionDate BETWEEN :start AND :end " +
            "GROUP BY t.seller.id " +
            "ORDER BY SUM(t.amount) DESC LIMIT 1")
    Optional<Long> findTopSellerId(LocalDateTime start, LocalDateTime end);

    /** Получает ids продавцов с суммой ниже порога за период. */
    @Query("SELECT t.seller.id FROM Transaction t" +
            " WHERE t.transactionDate BETWEEN :start AND :end" +
            " GROUP BY t.seller.id" +
            " HAVING SUM(t.amount) < :threshold")
    List<Long> findSellersUnderperforming(LocalDateTime start, LocalDateTime end, BigDecimal threshold);

    /** Получает дату лучшего дня (максимум транзакций) для продавца. */
    @Query("SELECT CAST(t.transactionDate AS DATE) FROM Transaction t " +
            "WHERE t.seller.id = :sellerId " +
            "GROUP BY CAST(t.transactionDate AS DATE) " +
            "ORDER BY COUNT(t.id) DESC")
    Optional<LocalDate> findBestDay(Long sellerId);

    /** Получает дату начала лучшей недели (максимум транзакций) для продавца. */
    @Query("SELECT FUNCTION('DATE_TRUNC', 'week', t.transactionDate) FROM Transaction t " +
            "WHERE t.seller.id = :sellerId " +
            "GROUP BY FUNCTION('DATE_TRUNC', 'week', t.transactionDate) " +
            "ORDER BY COUNT(t.id) DESC")
    Optional<LocalDate> findBestWeek(Long sellerId);

    /** Получает дату начала лучшего месяца (максимум транзакций) для продавца. */
    @Query("SELECT FUNCTION('DATE_TRUNC', 'month', t.transactionDate) FROM Transaction t " +
            "WHERE t.seller.id = :sellerId " +
            "GROUP BY FUNCTION('DATE_TRUNC', 'month', t.transactionDate) " +
            "ORDER BY COUNT(t.id) DESC")
    Optional<LocalDate> findBestMonth(Long sellerId);

    /** Получает дату начала лучшего квартала (максимум транзакций) для продавца. */
    @Query("SELECT FUNCTION('DATE_TRUNC', 'quarter', t.transactionDate) FROM Transaction t " +
            "WHERE t.seller.id = :sellerId " +
            "GROUP BY FUNCTION('DATE_TRUNC', 'quarter', t.transactionDate) " +
            "ORDER BY COUNT(t.id) DESC")
    Optional<LocalDate> findBestQuarter(Long sellerId);

    /** Получает дату начала лучшего года (максимум транзакций) для продавца. */
    @Query("SELECT FUNCTION('DATE_TRUNC', 'year', t.transactionDate) FROM Transaction t " +
            "WHERE t.seller.id = :sellerId " +
            "GROUP BY FUNCTION('DATE_TRUNC', 'year', t.transactionDate) " +
            "ORDER BY COUNT(t.id) DESC")
    Optional<LocalDate> findBestYear(Long sellerId);
}
