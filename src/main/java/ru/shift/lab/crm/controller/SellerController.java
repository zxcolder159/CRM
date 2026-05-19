package ru.shift.lab.crm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.shift.lab.crm.dto.BestPeriodResultDto;
import ru.shift.lab.crm.dto.CreateSellerDto;
import ru.shift.lab.crm.dto.SellerDto;
import ru.shift.lab.crm.dto.UpdateSellerDto;
import ru.shift.lab.crm.service.SellerService;
import ru.shift.lab.crm.util.PeriodType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Контроллер для работы с продавцами.
 * Предоставляет CRUD-операции, а также аналитику по периодам продаж.
 */
@Tag(name = "Продавцы", description = "Управление продавцами и аналитика по их продажам")
@RestController
@RequestMapping("/api/sellers")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    /**
     * Возвращает постраничный список всех активных продавцов.
     * Мягко удалённые продавцы в список не включаются.
     */
    @Operation(
            summary = "Список всех продавцов",
            description = "Возвращает постраничный список активных продавцов. Удалённые продавцы не включаются."
    )
    @ApiResponse(responseCode = "200", description = "Список успешно получен")
    @GetMapping
    public Page<SellerDto> getAllSellers(Pageable pageable) {
        return sellerService.getAllSellers(pageable);
    }

    /**
     * Возвращает продавца с наибольшей суммой транзакций за указанный период.
     * Начало периода нормализуется — например, для WEEK это всегда понедельник той же недели.
     */
    @Operation(
            summary = "Самый продуктивный продавец за период",
            description = "Находит продавца с наибольшей суммой транзакций за период, "
                    + "начинающийся от нормализованной даты startDate. "
                    + "Для WEEK начало сдвигается на понедельник, для MONTH — на 1-е число, "
                    + "для QUARTER — на начало квартала, для YEAR — на 1 января."
    )
    @ApiResponse(responseCode = "200", description = "Продавец найден")
    @ApiResponse(responseCode = "404", description = "Нет транзакций за указанный период")
    @GetMapping("/most-productive")
    public SellerDto getMostProductive(
            @Parameter(description = "Начало периода в формате ISO, например 2024-01-15T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Тип периода: DAY, WEEK, MONTH, QUARTER, YEAR")
            @RequestParam PeriodType periodType) {
        return sellerService.getMostProductiveSeller(startDate, periodType);
    }

    /**
     * Возвращает продавцов, чья суммарная выручка за период не достигла порога.
     * Продавцы без транзакций в указанном периоде тоже попадают в результат (их сумма считается нулём).
     */
    @Operation(
            summary = "Продавцы ниже порога выручки",
            description = "Возвращает постраничный список продавцов, у которых сумма транзакций "
                    + "за период строго меньше threshold. "
                    + "Продавцы без единой транзакции в периоде включаются с суммой 0."
    )
    @ApiResponse(responseCode = "200", description = "Список успешно получен")
    @GetMapping("/underperforming")
    public Page<SellerDto> getUnderperformingSellers(
            @Parameter(description = "Начало периода в формате ISO, например 2024-01-15T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Тип периода: DAY, WEEK, MONTH, QUARTER, YEAR")
            @RequestParam PeriodType periodType,
            @Parameter(description = "Порог суммы транзакций — продавцы строго ниже этого значения")
            @RequestParam BigDecimal threshold,
            Pageable pageable) {
        return sellerService.getUnderperformingSellers(startDate, periodType, threshold, pageable);
    }

    /**
     * Возвращает период, в котором у продавца было наибольшее количество транзакций.
     * Дата начала периода выровнена по границе: неделя с понедельника, квартал с 1-го числа первого месяца.
     */
    @Operation(
            summary = "Лучший период продаж продавца",
            description = "Определяет период с максимальным количеством транзакций для указанного продавца. "
                    + "Дата начала периода нормализована по границе единицы: "
                    + "неделя начинается с понедельника, квартал — с первого числа первого месяца квартала."
    )
    @ApiResponse(responseCode = "200", description = "Лучший период найден")
    @ApiResponse(responseCode = "404", description = "Продавец не найден или у него нет транзакций")
    @GetMapping("/{id}/best-period")
    public BestPeriodResultDto getBestPeriod(
            @Parameter(description = "ID продавца") @PathVariable Long id,
            @Parameter(description = "Тип периода: DAY, WEEK, MONTH, QUARTER, YEAR") @RequestParam PeriodType periodType) {
        return sellerService.getBestSalesPeriod(id, periodType);
    }

    /**
     * Создаёт нового продавца.
     * Дата регистрации проставляется автоматически на момент создания.
     */
    @Operation(
            summary = "Создать продавца",
            description = "Создаёт нового продавца. Дата регистрации проставляется сервером автоматически."
    )
    @ApiResponse(responseCode = "201", description = "Продавец успешно создан")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации входных данных")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SellerDto createSeller(@Valid @RequestBody CreateSellerDto createSellerDto) {
        return sellerService.createSeller(createSellerDto);
    }

    /**
     * Возвращает данные продавца по его идентификатору.
     */
    @Operation(
            summary = "Получить продавца по ID",
            description = "Возвращает полные данные продавца. Если продавец удалён или не существует — 404."
    )
    @ApiResponse(responseCode = "200", description = "Продавец найден")
    @ApiResponse(responseCode = "404", description = "Продавец не найден")
    @GetMapping("/{id}")
    public SellerDto getSellerById(@Parameter(description = "ID продавца") @PathVariable Long id) {
        return sellerService.getSellerById(id);
    }

    /**
     * Обновляет имя и контактную информацию продавца.
     */
    @Operation(
            summary = "Обновить данные продавца",
            description = "Заменяет имя и контактную информацию продавца на переданные значения."
    )
    @ApiResponse(responseCode = "200", description = "Данные успешно обновлены")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации входных данных")
    @ApiResponse(responseCode = "404", description = "Продавец не найден")
    @PutMapping("/{id}")
    public SellerDto updateSeller(
            @Parameter(description = "ID продавца") @PathVariable Long id,
            @Valid @RequestBody UpdateSellerDto updateSellerDto) {
        return sellerService.updateSeller(id, updateSellerDto);
    }

    /**
     * Мягко удаляет продавца — устанавливает флаг {@code isDeleted=true}.
     * Запись остаётся в базе данных и не появляется в обычных выборках.
     */
    @Operation(
            summary = "Удалить продавца",
            description = "Мягкое удаление: продавец помечается как удалённый и перестаёт отображаться, "
                    + "но физически из базы данных не удаляется."
    )
    @ApiResponse(responseCode = "204", description = "Продавец успешно удалён")
    @ApiResponse(responseCode = "404", description = "Продавец не найден")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSeller(@Parameter(description = "ID продавца") @PathVariable Long id) {
        sellerService.deleteSeller(id);
    }
}