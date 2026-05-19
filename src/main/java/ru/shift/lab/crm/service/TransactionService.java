package ru.shift.lab.crm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.shift.lab.crm.dto.CreateTransactionDto;
import ru.shift.lab.crm.dto.TransactionDto;
import ru.shift.lab.crm.entity.Transaction;
import ru.shift.lab.crm.exception.ResourceNotFoundException;
import ru.shift.lab.crm.repository.SellerRepository;
import ru.shift.lab.crm.repository.TransactionRepository;

import java.time.LocalDateTime;

/**
 * Сервис для управления транзакциями.
 * Содержит логику создания транзакций и их получения по различным критериям.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

    private final SellerRepository sellerRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Возвращает постраничный список всех транзакций в системе.
     */
    public Page<TransactionDto> getAllTransaction(Pageable pageable) {
        return transactionRepository.findAll(pageable).map(this::toDto);
    }

    /**
     * Возвращает транзакцию по её идентификатору.
     *
     * @throws ResourceNotFoundException если транзакция не найдена
     */
    public TransactionDto getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Транзакции с id " + id + " не найдено"));
        return toDto(transaction);
    }

    /**
     * Создаёт транзакцию и привязывает её к существующему продавцу.
     * Дата транзакции проставляется автоматически на момент вызова метода.
     *
     * @throws ResourceNotFoundException если продавец с указанным ID не найден
     */
    @Transactional
    public TransactionDto createTransaction(CreateTransactionDto createTransactionDto) {
        var seller = sellerRepository.findById(createTransactionDto.sellerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Продавец с id " + createTransactionDto.sellerId() + " не найден"));
        Transaction transaction = new Transaction();
        transaction.setAmount(createTransactionDto.amount());
        transaction.setPaymentType(createTransactionDto.paymentType());
        transaction.setSeller(seller);
        transaction.setTransactionDate(LocalDateTime.now());
        return toDto(transactionRepository.save(transaction));
    }

    /**
     * Возвращает постраничный список всех транзакций указанного продавца.
     * Перед выборкой проверяется существование продавца.
     *
     * @throws ResourceNotFoundException если продавец с указанным ID не найден
     */
    public Page<TransactionDto> getAllTransactionsBySellerId(Long id, Pageable pageable) {
        if (!sellerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Продавец с id " + id + " не найден");
        }
        return transactionRepository.findAllBySellerId(id, pageable).map(this::toDto);
    }

    /**
     * Преобразует сущность транзакции в DTO для передачи клиенту.
     */
    private TransactionDto toDto(Transaction transaction) {
        return new TransactionDto(
                transaction.getId(),
                transaction.getSeller().getId(),
                transaction.getAmount(),
                transaction.getPaymentType(),
                transaction.getTransactionDate()
        );
    }
}