package ru.shift.lab.crm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.shift.lab.crm.dto.CreateTransactionDto;
import ru.shift.lab.crm.dto.TransactionDto;
import ru.shift.lab.crm.service.TransactionService;

/**
 * Контроллер для работы с транзакциями.
 * Позволяет создавать транзакции и получать их по разным критериям.
 */
@Tag(name = "Транзакции", description = "Создание транзакций и получение их списков")
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Создаёт новую транзакцию для указанного продавца.
     * Дата транзакции проставляется автоматически на момент создания.
     */
    @Operation(
            summary = "Создать транзакцию",
            description = "Создаёт транзакцию и привязывает её к продавцу по sellerId. "
                    + "Дата транзакции проставляется сервером автоматически."
    )
    @ApiResponse(responseCode = "201", description = "Транзакция успешно создана")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации входных данных")
    @ApiResponse(responseCode = "404", description = "Продавец с указанным ID не найден")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionDto createTransaction(@Valid @RequestBody CreateTransactionDto createTransactionDto) {
        return transactionService.createTransaction(createTransactionDto);
    }

    /**
     * Возвращает постраничный список всех транзакций указанного продавца.
     */
    @Operation(
            summary = "Транзакции продавца",
            description = "Возвращает все транзакции продавца с пагинацией. "
                    + "Если продавец не существует — возвращает 404."
    )
    @ApiResponse(responseCode = "200", description = "Список успешно получен")
    @ApiResponse(responseCode = "404", description = "Продавец не найден")
    @GetMapping("/seller/{sellerId}")
    public Page<TransactionDto> getTransactionsBySellerId(
            @Parameter(description = "ID продавца") @PathVariable Long sellerId,
            Pageable pageable) {
        return transactionService.getAllTransactionsBySellerId(sellerId, pageable);
    }

    /**
     * Возвращает постраничный список всех транзакций в системе.
     */
    @Operation(
            summary = "Список всех транзакций",
            description = "Возвращает полный постраничный список транзакций по всем продавцам."
    )
    @ApiResponse(responseCode = "200", description = "Список успешно получен")
    @GetMapping
    public Page<TransactionDto> getAllTransactions(Pageable pageable) {
        return transactionService.getAllTransaction(pageable);
    }

    /**
     * Возвращает транзакцию по её идентификатору.
     */
    @Operation(
            summary = "Получить транзакцию по ID",
            description = "Возвращает данные конкретной транзакции. Если транзакция не найдена — возвращает 404."
    )
    @ApiResponse(responseCode = "200", description = "Транзакция найдена")
    @ApiResponse(responseCode = "404", description = "Транзакция не найдена")
    @GetMapping("/{id}")
    public TransactionDto getTransactionById(@Parameter(description = "ID транзакции") @PathVariable Long id) {
        return transactionService.getTransactionById(id);
    }
}