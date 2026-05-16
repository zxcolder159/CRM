package ru.shift.lab.crm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.shift.lab.crm.entity.Seller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Репозиторий Продавца. */
@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {

    /** Возвращает продавцов с суммой транзакций ниже порога за период (включая 0). */
    @Query("SELECT s FROM Seller s " +
            "LEFT JOIN Transaction t ON t.seller = s AND t.transactionDate BETWEEN :start AND :end " +
            "GROUP BY s " +
            "HAVING COALESCE(SUM(t.amount), 0) < :threshold")
    List<Seller> findUnderperformingSellers(LocalDateTime start, LocalDateTime end, BigDecimal threshold);
}
