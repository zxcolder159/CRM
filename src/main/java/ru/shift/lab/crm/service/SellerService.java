package ru.shift.lab.crm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.shift.lab.crm.dto.BestPeriodResultDto;
import ru.shift.lab.crm.dto.CreateSellerDto;
import ru.shift.lab.crm.dto.SellerDto;
import ru.shift.lab.crm.dto.UpdateSellerDto;
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
    public SellerDto getMostProductiveSeller(LocalDateTime startDate, PeriodType periodType) {
        LocalDateTime endDate = calculatePeriodEndDate(startDate, periodType);
        Seller seller = transactionRepository.findTopSellerId(startDate, endDate)
                .flatMap(sellerRepository::findById)
                .orElseThrow(() -> new ResourceNotFoundException("Нет транзакций за указанный период или продавец не найден"));
        return toDto(seller);
    }

    /** Возвращает продавцов с суммой ниже порога за период. */
    public List<SellerDto> getUnderperformingSellers(LocalDateTime startDate, PeriodType periodType, BigDecimal threshold) {
        LocalDateTime endDate = calculatePeriodEndDate(startDate, periodType);
        List<Seller> sellers = sellerRepository.findUnderperformingSellers(startDate, endDate, threshold);
        return sellers.stream()
                .map(this::toDto)
                .toList();
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

    public List<SellerDto> getAllSellers() {
        return sellerRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public SellerDto getSellerById(Long id) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Продавец с id " + id + " не найден"));
        return toDto(seller);
    }

    @Transactional
    public SellerDto createSeller(CreateSellerDto createSellerDto) {
        Seller seller = new Seller();
        seller.setName(createSellerDto.name());
        seller.setContactInfo(createSellerDto.contactInfo());
        seller.setRegistrationDate(LocalDateTime.now());
        Seller saved = sellerRepository.save(seller);
        return toDto(saved);
    }

    @Transactional
    public SellerDto updateSeller(Long id, UpdateSellerDto updateSellerDto) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Продавец с id " + id + " не найден"));
        seller.setName(updateSellerDto.name());
        seller.setContactInfo(updateSellerDto.contactInfo());
        return toDto(seller);
    }

    @Transactional
    public void deleteSeller(Long id) {
        Seller seller = sellerRepository.findById(id)
                        .orElseThrow(()-> new ResourceNotFoundException("Продавец с id " + id + " не найден"));
        seller.setDeleted(true);
    }

    /** Преобразует Entity в DTO. */
    private SellerDto toDto(Seller seller) {
        return new SellerDto(
                seller.getId(),
                seller.getName(),
                seller.getContactInfo(),
                seller.getRegistrationDate()
        );
    }

    /** Вычисляет дату конца периода на основе стартовой даты и типа периода. */
    private LocalDateTime calculatePeriodEndDate(LocalDateTime startDate, PeriodType periodType) {
        LocalDate startDateOnly = startDate.toLocalDate();
        LocalDate endDate = switch (periodType) {
            case DAY -> startDateOnly;
            case WEEK -> startDateOnly.plusDays(6);
            case MONTH -> startDateOnly.plusMonths(1).minusDays(1);
            case QUARTER -> startDateOnly.plusMonths(3).minusDays(1);
            case YEAR -> startDateOnly.plusYears(1).minusDays(1);
        };
        return LocalDateTime.of(endDate, LocalTime.MAX);
    }
}
