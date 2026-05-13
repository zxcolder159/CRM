package ru.shift.lab.crm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.shift.lab.crm.entity.Seller;
import ru.shift.lab.crm.exception.ResourceNotFoundException;
import ru.shift.lab.crm.repository.SellerRepository;
import ru.shift.lab.crm.repository.TransactionRepository;

import java.time.LocalDateTime;

/** Сервис для Продавца. */
@Service
@RequiredArgsConstructor
public class SellerService {
    private final SellerRepository sellerRepository;
    private final TransactionRepository transactionRepository;

    /** Возвращает самого продуктивного продавца за указанный период. */
    public Seller getMostProductiveSeller(LocalDateTime start, LocalDateTime end) {
        var sellersIds = transactionRepository.findTopSellerId(start, end);
        if (sellersIds.isEmpty()) {
            throw new ResourceNotFoundException("Нет транзакций за указанный период");
        }

        return sellerRepository.findById(sellersIds.getFirst())
                .orElseThrow(() -> new ResourceNotFoundException("Продавец не найден"));
    }

}
