package ru.shift.lab.crm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.shift.lab.crm.dto.CreateTransactionDto;
import ru.shift.lab.crm.dto.TransactionDto;
import ru.shift.lab.crm.entity.Transaction;
import ru.shift.lab.crm.exception.ResourceNotFoundException;
import ru.shift.lab.crm.repository.SellerRepository;
import ru.shift.lab.crm.repository.TransactionRepository;

import java.time.LocalDateTime;
import java.util.List;

/** Сервис для работы с транзакциями. */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class TransactionService {
    private final SellerRepository sellerRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Возвращает все транзакции.
     */
    public List<TransactionDto> getAllTransaction() {
        return transactionRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Возвращает транзакцию по id.
     */
    public TransactionDto getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Транзакции с id " + id + " не найдено"));
        return toDto(transaction);
    }

    /**
     * Создает транзакцию для продавца.
     */
    @Transactional
    public TransactionDto createTransaction(CreateTransactionDto createTransactionDto) {
        var seller = sellerRepository.findById(createTransactionDto.sellerId())
                .orElseThrow(() -> new ResourceNotFoundException("Продавец с id " + createTransactionDto.sellerId() + " не найден"));
        Transaction transaction = new Transaction();
        transaction.setAmount(createTransactionDto.amount());
        transaction.setPaymentType(createTransactionDto.paymentType());
        transaction.setSeller(seller);
        transaction.setTransactionDate(LocalDateTime.now());
        Transaction saved = transactionRepository.save(transaction);
        return toDto(saved);
    }

    /**
     * Возвращает все транзакции продавца по его id.
     */
    public List<TransactionDto> getAllTransactionsBySellerId(Long id) {
        if (!sellerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Продавец с id " + id + " не найден");
        }
        return transactionRepository.findAllBySellerId(id).stream()
                .map(this::toDto)
                .toList();
    }

    /** Преобразует Entity в DTO. */
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
