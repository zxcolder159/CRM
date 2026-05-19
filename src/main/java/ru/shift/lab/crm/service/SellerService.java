package ru.shift.lab.crm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

/**
 * Сервис для управления продавцами.
 * Содержит бизнес-логику CRUD-операций и аналитику по периодам продаж.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerService {

    private final SellerRepository sellerRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Возвращает постраничный список всех активных продавцов.
     */
    public Page<SellerDto> getAllSellers(Pageable pageable) {
        return sellerRepository.findAll(pageable).map(this::toDto);
    }

    /**
     * Возвращает продавца по его идентификатору.
     *
     * @throws ResourceNotFoundException если продавец не найден
     */
    public SellerDto getSellerById(Long id) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Продавец с id " + id + " не найден"));
        return toDto(seller);
    }

    /**
     * Создаёт нового продавца.
     * Дата регистрации проставляется автоматически на момент вызова метода.
     */
    @Transactional
    public SellerDto createSeller(CreateSellerDto createSellerDto) {
        Seller seller = new Seller();
        seller.setName(createSellerDto.name());
        seller.setContactInfo(createSellerDto.contactInfo());
        seller.setRegistrationDate(LocalDateTime.now());
        return toDto(sellerRepository.save(seller));
    }

    /**
     * Обновляет имя и контактную информацию продавца.
     *
     * @throws ResourceNotFoundException если продавец не найден
     */
    @Transactional
    public SellerDto updateSeller(Long id, UpdateSellerDto updateSellerDto) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Продавец с id " + id + " не найден"));
        seller.setName(updateSellerDto.name());
        seller.setContactInfo(updateSellerDto.contactInfo());
        sellerRepository.save(seller);
        return toDto(seller);
    }

    /**
     * Мягко удаляет продавца — выставляет флаг {@code isDeleted=true}.
     * Запись остаётся в базе данных, но перестаёт возвращаться в обычных запросах.
     *
     * @throws ResourceNotFoundException если продавец не найден
     */
    @Transactional
    public void deleteSeller(Long id) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Продавец с id " + id + " не найден"));
        seller.setDeleted(true);
        sellerRepository.save(seller);
    }

    /**
     * Возвращает продавца с наибольшей суммой транзакций за указанный период.
     * <p>
     * Переданная дата нормализуется к границе периода: для WEEK — к понедельнику,
     * для MONTH — к первому числу, для QUARTER — к первому числу квартала,
     * для YEAR — к 1 января. Конец периода вычисляется относительно нормализованного начала.
     *
     * @throws ResourceNotFoundException если за период нет ни одной транзакции
     */
    public SellerDto getMostProductiveSeller(LocalDateTime startDate, PeriodType periodType) {
        LocalDateTime normalizedStart = calculatePeriodStartDate(startDate, periodType);
        LocalDateTime endDate = calculatePeriodEndDate(startDate, periodType);
        Seller seller = transactionRepository.findTopSellerId(normalizedStart, endDate)
                .flatMap(sellerRepository::findById)
                .orElseThrow(() -> new ResourceNotFoundException("Нет транзакций за указанный период или продавец не найден"));
        return toDto(seller);
    }

    /**
     * Возвращает постраничный список продавцов, чья суммарная выручка за период
     * строго меньше порогового значения.
     * <p>
     * Продавцы без транзакций в указанном периоде включаются в результат,
     * их сумма считается равной нулю.
     */
    public Page<SellerDto> getUnderperformingSellers(LocalDateTime startDate, PeriodType periodType,
                                                     BigDecimal threshold, Pageable pageable) {
        LocalDateTime normalizedStart = calculatePeriodStartDate(startDate, periodType);
        LocalDateTime endDate = calculatePeriodEndDate(startDate, periodType);
        return sellerRepository.findUnderperformingSellers(normalizedStart, endDate, threshold, pageable)
                .map(this::toDto);
    }

    /**
     * Возвращает период, в котором у продавца было наибольшее количество транзакций.
     * <p>
     * Сначала проверяется существование продавца, затем по базе ищется лучший период
     * для запрошенного типа. Начало периода нормализуется к границе единицы: неделя
     * начинается с понедельника, квартал — с первого числа первого месяца квартала.
     *
     * @throws ResourceNotFoundException если продавец не найден или транзакций нет
     */
    public BestPeriodResultDto getBestSalesPeriod(Long sellerId, PeriodType periodType) {
        sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Продавец с id " + sellerId + " не найден"));

        LocalDate periodStart = switch (periodType) {
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

        LocalDate periodEnd = calculatePeriodEndDate(
                LocalDateTime.of(periodStart, LocalTime.MIDNIGHT), periodType
        ).toLocalDate();

        return new BestPeriodResultDto(sellerId, periodStart, periodEnd, periodType);
    }

    /**
     * Вычисляет нормализованное начало периода по переданной дате.
     * <p>
     * Правила нормализации:
     * <ul>
     *   <li>DAY — та же дата, время 00:00:00</li>
     *   <li>WEEK — ближайший предшествующий понедельник (или сама дата, если понедельник)</li>
     *   <li>MONTH — первое число того же месяца</li>
     *   <li>QUARTER — первое число первого месяца текущего квартала</li>
     *   <li>YEAR — 1 января того же года</li>
     * </ul>
     */
    private LocalDateTime calculatePeriodStartDate(LocalDateTime date, PeriodType periodType) {
        LocalDate dateOnly = date.toLocalDate();
        LocalDate startDate = switch (periodType) {
            case DAY -> dateOnly;
            case WEEK -> dateOnly.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MONTH -> dateOnly.with(TemporalAdjusters.firstDayOfMonth());
            case QUARTER -> dateOnly
                    .withMonth(((dateOnly.getMonthValue() - 1) / 3) * 3 + 1)
                    .with(TemporalAdjusters.firstDayOfMonth());
            case YEAR -> dateOnly.with(TemporalAdjusters.firstDayOfYear());
        };
        return LocalDateTime.of(startDate, LocalTime.MIDNIGHT);
    }

    /**
     * Вычисляет конец периода по стартовой дате и типу периода.
     * <p>
     * Возвращаемое время всегда {@code 23:59:59.999999999} последнего дня периода,
     * чтобы граничные транзакции попадали в выборку.
     */
    private LocalDateTime calculatePeriodEndDate(LocalDateTime startDate, PeriodType periodType) {
        LocalDate startDateOnly = startDate.toLocalDate();
        LocalDate endDate = switch (periodType) {
            case DAY -> startDateOnly;
            case WEEK -> startDateOnly.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            case MONTH -> startDateOnly.with(TemporalAdjusters.lastDayOfMonth());
            case QUARTER -> startDateOnly
                    .withMonth(((startDateOnly.getMonthValue() - 1) / 3 + 1) * 3)
                    .with(TemporalAdjusters.lastDayOfMonth());
            case YEAR -> startDateOnly.with(TemporalAdjusters.lastDayOfYear());
        };
        return LocalDateTime.of(endDate, LocalTime.MAX);
    }

    /**
     * Преобразует сущность продавца в DTO для передачи клиенту.
     */
    private SellerDto toDto(Seller seller) {
        return new SellerDto(
                seller.getId(),
                seller.getName(),
                seller.getContactInfo(),
                seller.getRegistrationDate()
        );
    }
}