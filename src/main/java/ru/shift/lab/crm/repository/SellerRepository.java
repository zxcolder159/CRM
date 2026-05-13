package ru.shift.lab.crm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.shift.lab.crm.entity.Seller;

/** Репозиторий Продавца. */
@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {

}
