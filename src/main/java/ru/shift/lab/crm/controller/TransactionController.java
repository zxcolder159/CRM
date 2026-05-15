package ru.shift.lab.crm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.shift.lab.crm.dto.CreateTransactionDto;
import ru.shift.lab.crm.dto.TransactionDto;
import ru.shift.lab.crm.service.TransactionService;

import jakarta.validation.Valid;
import java.util.List;

/** REST контроллер для управления транзакциями. */
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
    public List<TransactionDto> getAllTransactions() {
        return transactionService.getAllTransaction();
    }

    /** Получить информацию о транзакции по ID. */
    @GetMapping("/{id}")
    public TransactionDto getTransactionById(@PathVariable Long id) {
        return transactionService.getTransactionById(id);
    }

}
