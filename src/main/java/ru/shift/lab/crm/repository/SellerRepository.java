package ru.shift.lab.crm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.shift.lab.crm.entity.Seller;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Репозиторий для работы с продавцами.
 * Помимо стандартных CRUD-операций содержит аналитический запрос
 * для поиска продавцов, не достигших порогового значения выручки.
 */
@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {

    /**
     * Возвращает постраничный список продавцов, у которых суммарная выручка
     * за указанный период строго меньше порогового значения.
     * <p>
     * Продавцы без транзакций в периоде включаются в результат —
     * их сумма считается нулём через {@code COALESCE}.
     * Для корректного подсчёта страниц используется отдельный {@code countQuery},
     * так как основной запрос содержит {@code GROUP BY} и {@code JOIN}.
     *
     * @param start     начало периода (включительно)
     * @param end       конец периода (включительно)
     * @param threshold порог суммы, ниже которого продавец попадает в выборку
     * @param pageable  параметры пагинации и сортировки
     */
    @Query(value = "SELECT s FROM Seller s " +
            "LEFT JOIN Transaction t ON t.seller = s AND t.transactionDate BETWEEN :start AND :end " +
            "GROUP BY s " +
            "HAVING COALESCE(SUM(t.amount), 0) < :threshold",
           countQuery = "SELECT COUNT(s.id) FROM Seller s " +
            "WHERE COALESCE((SELECT SUM(t.amount) FROM Transaction t WHERE t.seller = s " +
            "AND t.transactionDate BETWEEN :start AND :end), 0) < :threshold")
    Page<Seller> findUnderperformingSellers(LocalDateTime start, LocalDateTime end,
                                            BigDecimal threshold, Pageable pageable);
}