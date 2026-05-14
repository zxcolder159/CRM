package ru.shift.lab.crm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.shift.lab.crm.dto.BestPeriodResultDto;
import ru.shift.lab.crm.entity.Seller;
import ru.shift.lab.crm.exception.ResourceNotFoundException;
import ru.shift.lab.crm.repository.SellerRepository;
import ru.shift.lab.crm.repository.TransactionRepository;
import ru.shift.lab.crm.util.PeriodType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Сервис для Продавца. */
@Service
@RequiredArgsConstructor
public class SellerService {
    private final SellerRepository sellerRepository;
    private final TransactionRepository transactionRepository;

    /** Возвращает самого продуктивного продавца за указанный период. */
    public Seller getMostProductiveSeller(LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findTopSellerId(start, end)
                .flatMap(sellerRepository::findById)
                .orElseThrow(() -> new ResourceNotFoundException("Нет транзакций за указанный период или продавец не найден"));
    }

    /** Возвращает продавцов с суммой ниже порога за период. */
    public List<Seller> getUnderperformingSellers(LocalDateTime start, LocalDateTime end, BigDecimal threshold) {
        var sellersIds = transactionRepository.findSellersUnderperforming(start, end, threshold);
        if (sellersIds.isEmpty()) {
            throw new ResourceNotFoundException("Нет продавцов с производительностью ниже " + threshold);
        }

        return sellerRepository.findAllById(sellersIds);
    }

    /** Возвращает лучший период продаж продавца и проверяет его существование. */
    public BestPeriodResultDto getBestSalesPeriod(Long sellerId, PeriodType periodType) {
        sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Продавец с id " + sellerId + " не найден"));

        LocalDate bestPeriod = switch (periodType) {
            case DAY -> transactionRepository.findBestDay(sellerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Нет транзакций для дня"));
            case WEEK -> transactionRepository.findBestWeek(sellerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Нет транзакций для недели"));
            case MONTH -> transactionRepository.findBestMonth(sellerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Нет транзакций для месяца"));
            case QUARTER -> transactionRepository.findBestQuarter(sellerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Нет транзакций для квартала"));
            case YEAR -> transactionRepository.findBestYear(sellerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Нет транзакций для года"));
        };

        return new BestPeriodResultDto(sellerId, bestPeriod, periodType);
    }
}
