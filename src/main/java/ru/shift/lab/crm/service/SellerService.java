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
import java.util.List;

/** Сервис для Продавца. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerService {
    private final SellerRepository sellerRepository;
    private final TransactionRepository transactionRepository;

    /** Возвращает самого продуктивного продавца за указанный период. */
    public SellerDto getMostProductiveSeller(LocalDateTime start, LocalDateTime end) {
        Seller seller = transactionRepository.findTopSellerId(start, end)
                .flatMap(sellerRepository::findById)
                .orElseThrow(() -> new ResourceNotFoundException("Нет транзакций за указанный период или продавец не найден"));
        return toDto(seller);
    }

    /** Возвращает продавцов с суммой ниже порога за период. */
    public List<SellerDto> getUnderperformingSellers(LocalDateTime start, LocalDateTime end, BigDecimal threshold) {
        List<Seller> sellers = sellerRepository.findAllById(transactionRepository
                .findSellersUnderperforming(start, end, threshold));
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
        Seller saved = sellerRepository.save(seller);
        return toDto(saved);
    }

    @Transactional
    public void deleteSeller(Long id) {
        if (!sellerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Продавец с id " + id + " не найден");
        }
        sellerRepository.deleteById(id);
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
}
