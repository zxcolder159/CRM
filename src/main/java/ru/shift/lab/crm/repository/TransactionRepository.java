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

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findAllBySellerId(Long sellerId, Pageable pageable);

    @Query("SELECT t.seller.id FROM Transaction t " +
            "JOIN t.seller s " +
            "WHERE t.transactionDate BETWEEN :start AND :end " +
            "AND s.isDeleted = false " +
            "GROUP BY t.seller.id " +
            "ORDER BY SUM(t.amount) DESC LIMIT 1")
    Optional<Long> findTopSellerId(LocalDateTime start, LocalDateTime end);

    @Query("SELECT CAST(t.transactionDate AS DATE) FROM Transaction t " +
            "WHERE t.seller.id = :sellerId " +
            "GROUP BY CAST(t.transactionDate AS DATE) " +
            "ORDER BY COUNT(t.id) DESC LIMIT 1")
    Optional<LocalDate> findBestDay(Long sellerId);

    @Query("SELECT CAST(FUNCTION('DATE_TRUNC', 'week', t.transactionDate) AS DATE) FROM Transaction t " +
            "WHERE t.seller.id = :sellerId " +
            "GROUP BY CAST(FUNCTION('DATE_TRUNC', 'week', t.transactionDate) AS DATE) " +
            "ORDER BY COUNT(t.id) DESC LIMIT 1")
    Optional<LocalDate> findBestWeek(Long sellerId);

    @Query("SELECT CAST(FUNCTION('DATE_TRUNC', 'month', t.transactionDate) AS DATE) FROM Transaction t " +
            "WHERE t.seller.id = :sellerId " +
            "GROUP BY CAST(FUNCTION('DATE_TRUNC', 'month', t.transactionDate) AS DATE) " +
            "ORDER BY COUNT(t.id) DESC LIMIT 1")
    Optional<LocalDate> findBestMonth(Long sellerId);

    @Query("SELECT CAST(FUNCTION('DATE_TRUNC', 'quarter', t.transactionDate) AS DATE) FROM Transaction t " +
            "WHERE t.seller.id = :sellerId " +
            "GROUP BY CAST(FUNCTION('DATE_TRUNC', 'quarter', t.transactionDate) AS DATE) " +
            "ORDER BY COUNT(t.id) DESC LIMIT 1")
    Optional<LocalDate> findBestQuarter(Long sellerId);

    @Query("SELECT CAST(FUNCTION('DATE_TRUNC', 'year', t.transactionDate) AS DATE) FROM Transaction t " +
            "WHERE t.seller.id = :sellerId " +
            "GROUP BY CAST(FUNCTION('DATE_TRUNC', 'year', t.transactionDate) AS DATE) " +
            "ORDER BY COUNT(t.id) DESC LIMIT 1")
    Optional<LocalDate> findBestYear(Long sellerId);
}
