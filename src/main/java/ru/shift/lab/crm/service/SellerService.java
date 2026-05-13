package ru.shift.lab.crm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.shift.lab.crm.entity.Seller;
import ru.shift.lab.crm.exception.ResourceNotFoundException;
import ru.shift.lab.crm.repository.SellerRepository;
import ru.shift.lab.crm.repository.TransactionRepository;

import java.time.LocalDateTime;

/**
 * Сервис для работы с продавцами.
 * Содержит бизнес-логику для управления продавцами и их аналитикой.
 */
@Service
@RequiredArgsConstructor
public class SellerService {
    private final SellerRepository sellerRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Получает самого продуктивного продавца за указанный период.
     *
     * @param start начало периода в формате LocalDateTime
     * @param end   конец периода в формате LocalDateTime
     * @return объект Seller самого продуктивного продавца
     * @throws ResourceNotFoundException если нет транзакций за период или продавец не найден
     */
    public Seller getMostProductiveSeller(LocalDateTime start, LocalDateTime end) {
        var sellersIds = transactionRepository.findTopSellerId(start, end);
        if (sellersIds.isEmpty()) {
            throw new ResourceNotFoundException("Нет транзакций за указанный период");
        }

        return sellerRepository.findById(sellersIds.getFirst())
                .orElseThrow(() -> new ResourceNotFoundException("Продавец не найден"));
    }

}
