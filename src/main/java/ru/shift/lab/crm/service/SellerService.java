package ru.shift.lab.crm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.shift.lab.crm.dto.BestPeriodResultDto;
import ru.shift.lab.crm.entity.Seller;
import ru.shift.lab.crm.exception.ResourceNotFoundException;
import ru.shift.lab.crm.repository.SellerRepository;
import ru.shift.lab.crm.repository.TransactionRepository;
import ru.shift.lab.crm.util.PeriodType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/** Сервис для Продавца. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
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
        return sellerRepository.findAllById(transactionRepository
                .findSellersUnderperforming(start, end, threshold));
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

    public List<Seller> getAllSellers() {
        return sellerRepository.findAll();
    }

    public Seller getSellerById(Long id) {
        return sellerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Продавец с id " + id + " не найден"));
    }

    @Transactional
    public Seller createSeller(String name, String contactInfo) {
        Seller seller = new Seller();
        seller.setName(name);
        seller.setContactInfo(contactInfo);
        seller.setRegistrationDate(LocalDateTime.now());
        return sellerRepository.save(seller);
    }

    @Transactional
    public Seller updateSeller(Long id, String name, String contactInfo) {
        Seller seller = getSellerById(id);
        seller.setName(name);
        seller.setContactInfo(contactInfo);
        return sellerRepository.save(seller);
    }

    @Transactional
    public void deleteSeller(Long id) {
        if (!sellerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Продавец с id " + id + " не найден");
        }
        sellerRepository.deleteById(id);
    }


}
