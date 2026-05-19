package ru.shift.lab.crm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.shift.lab.crm.entity.Seller;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {

    @Query(value = "SELECT s FROM Seller s " +
            "LEFT JOIN Transaction t ON t.seller = s AND t.transactionDate BETWEEN :start AND :end " +
            "GROUP BY s " +
            "HAVING COALESCE(SUM(t.amount), 0) < :threshold",
           countQuery = "SELECT COUNT(s.id) FROM Seller s " +
            "WHERE COALESCE((SELECT SUM(t.amount) FROM Transaction t WHERE t.seller = s " +
            "AND t.transactionDate BETWEEN :start AND :end), 0) < :threshold")
    Page<Seller> findUnderperformingSellers(LocalDateTime start, LocalDateTime end, BigDecimal threshold, Pageable pageable);
}