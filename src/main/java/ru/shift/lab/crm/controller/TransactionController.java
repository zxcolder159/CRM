package ru.shift.lab.crm.controller;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.shift.lab.crm.dto.CreateTransactionDto;
import ru.shift.lab.crm.dto.TransactionDto;
import ru.shift.lab.crm.service.TransactionService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jakarta.validation.Valid;
import java.util.List;

@Tag(name = "Transactions")
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    /** Создать новую транзакцию. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionDto createTransaction(@Valid @RequestBody CreateTransactionDto createTransactionDto) {
        return transactionService.createTransaction(createTransactionDto);
    }

    /** Получить все транзакции продавца по его ID. */
    @GetMapping("/seller/{sellerId}")
    public List<TransactionDto> getTransactionsBySellerId(@PathVariable Long sellerId) {
        return transactionService.getAllTransactionsBySellerId(sellerId);
    }

    /** Получить список всех транзакций. */
    @GetMapping
    public Page<TransactionDto> getAllTransactions(Pageable pageable) {
        return transactionService.getAllTransaction(pageable);
    }

    @ApiResponse(responseCode = "404", description = "Транзакция не найдена")
    @GetMapping("/{id}")
    public TransactionDto getTransactionById(@PathVariable Long id) {
        return transactionService.getTransactionById(id);
    }

}
