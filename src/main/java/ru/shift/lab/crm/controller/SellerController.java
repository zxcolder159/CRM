package ru.shift.lab.crm.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.shift.lab.crm.dto.BestPeriodResultDto;
import ru.shift.lab.crm.dto.CreateSellerDto;
import ru.shift.lab.crm.dto.SellerDto;
import ru.shift.lab.crm.dto.UpdateSellerDto;
import ru.shift.lab.crm.service.SellerService;
import ru.shift.lab.crm.util.PeriodType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Sellers")
@RestController
@RequestMapping("/api/sellers")
@RequiredArgsConstructor
public class SellerController {
    private final SellerService sellerService;

    /** Получить список всех продавцов. */
    @GetMapping
    public Page<SellerDto> getAllSellers(Pageable pageable) {
        return sellerService.getAllSellers(pageable);
    }

    @Operation(summary = "Самый продуктивный продавец за период",
            description = "Период: [startDate, startDate + periodType]. Считается по сумме транзакций.")
    @GetMapping("/most-productive")
    public SellerDto getMostProductive(
            @Parameter(description = "Начало периода") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Длина периода: DAY, WEEK, MONTH, QUARTER, YEAR") @RequestParam PeriodType periodType) {
        return sellerService.getMostProductiveSeller(startDate, periodType);
    }

    @Operation(summary = "Продавцы ниже порога",
            description = "Возвращает продавцов, чья сумма транзакций за период строго меньше threshold. Продавцы без транзакций включаются.")
    @GetMapping("/underperforming")
    public List<SellerDto> getUnderperformingSellers(
            @Parameter(description = "Начало периода") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Длина периода: DAY, WEEK, MONTH, QUARTER, YEAR") @RequestParam PeriodType periodType,
            @Parameter(description = "Порог суммы транзакций за период") @RequestParam BigDecimal threshold) {
        return sellerService.getUnderperformingSellers(startDate, periodType, threshold);
    }

    @Operation(summary = "Лучший период продаж продавца",
            description = "Возвращает период с наибольшим количеством транзакций. periodStart — начало периода по ISO (неделя с понедельника, квартал с 1-го числа).")
    @ApiResponse(responseCode = "404", description = "Продавец не найден")
    @GetMapping("/{id}/best-period")
    public BestPeriodResultDto getBestPeriod(@PathVariable Long id, @RequestParam PeriodType periodType) {
        return sellerService.getBestSalesPeriod(id, periodType);
    }

    /** Создать нового продавца. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SellerDto createSeller(@Valid @RequestBody CreateSellerDto createSellerDto) {
        return sellerService.createSeller(createSellerDto);
    }

    @ApiResponse(responseCode = "404", description = "Продавец не найден")
    @GetMapping("/{id}")
    public SellerDto getSellerById(@PathVariable Long id) {
        return sellerService.getSellerById(id);
    }

    /** Обновить информацию о продавце. */
    @PutMapping("/{id}")
    public SellerDto updateSeller(@PathVariable Long id, @Valid @RequestBody UpdateSellerDto updateSellerDto) {
        return sellerService.updateSeller(id, updateSellerDto);
    }

    @Operation(summary = "Удалить продавца", description = "Мягкое удаление: устанавливает isDeleted=true, запись остаётся в БД.")
    @ApiResponse(responseCode = "404", description = "Продавец не найден")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSeller(@PathVariable Long id) {
        sellerService.deleteSeller(id);
    }
}
