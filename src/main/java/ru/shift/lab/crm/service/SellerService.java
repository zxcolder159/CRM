package ru.shift.lab.crm.service;

import org.springframework.stereotype.Service;
import ru.shift.lab.crm.entity.Seller;
import ru.shift.lab.crm.exception.ResourseNotFoundException;
import ru.shift.lab.crm.repository.SellerRepository;
import ru.shift.lab.crm.repository.TransactionRepository;

import java.time.LocalDateTime;

@Service
public class SellerService {
    private SellerRepository sellerRepository;
    private TransactionRepository transactionRepository;
    public Seller getMostProductiveSeller(LocalDateTime start, LocalDateTime end) {
        var sellersIds = transactionRepository.findTopSellerId(start, end);
        if (sellersIds.isEmpty()) {
            throw new ResourseNotFoundException("Нет транзакций за указанный период");
        }

        return sellerRepository.findById(sellersIds.getFirst())
                .orElseThrow(() -> new ResourseNotFoundException("Продавец не найден"));
    }

}
