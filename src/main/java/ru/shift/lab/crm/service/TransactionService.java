package ru.shift.lab.crm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.shift.lab.crm.entity.Transaction;
import ru.shift.lab.crm.exception.ResourceNotFoundException;
import ru.shift.lab.crm.repository.SellerRepository;
import ru.shift.lab.crm.repository.TransactionRepository;

import java.math.BigDecimal;
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
    public List<Transaction> getAllTransaction() {
        return transactionRepository.findAll();
    }

    /**
     * Возвращает транзакцию по id.
     */
    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Транзакции с id " + id + " не найдено"));
    }

    /**
     * Создает транзакцию для продавца по его id, сумме и типу оплаты.
     */
    @Transactional
    public Transaction createTransaction(Long sellerId, BigDecimal amount, String paymentType) {
        var seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Продавец с id " + sellerId + " не найден"));
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setPaymentType(paymentType);
        transaction.setSeller(seller);
        transaction.setTransactionDate(LocalDateTime.now());
        return transactionRepository.save(transaction);
    }

    /**
     * Возвращает все транзакции продавца по его id.
     */
    public List<Transaction> getAllTransactionsBySellerId(Long id) {
        if (!sellerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Продавец с id " + id + " не найден");
        }
        return transactionRepository.findAllBySellerId(id);
    }
}
