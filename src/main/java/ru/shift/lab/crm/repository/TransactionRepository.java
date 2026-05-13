package ru.shift.lab.crm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.shift.lab.crm.entity.Transaction;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Репозиторий для работы с сущностью транзакция.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findAllBySellerId(Long sellerId);

    @Query("SELECT t.seller.id FROM Transaction t " +
            "WHERE t.transactionDate BETWEEN :start AND :end " +
            "GROUP BY t.seller.id " +
            "ORDER BY SUM(t.amount) DESC LIMIT 1")
    List<Long> findTopSellerId(LocalDateTime start, LocalDateTime end);
}
