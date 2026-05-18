package ru.shift.lab.crm.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.shift.lab.crm.dto.CreateTransactionDto;
import ru.shift.lab.crm.dto.TransactionDto;
import ru.shift.lab.crm.entity.Seller;
import ru.shift.lab.crm.entity.Transaction;
import ru.shift.lab.crm.exception.ResourceNotFoundException;
import ru.shift.lab.crm.repository.SellerRepository;
import ru.shift.lab.crm.repository.TransactionRepository;
import ru.shift.lab.crm.util.PaymentType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private SellerRepository sellerRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Seller seller(Long id) {
        Seller s = new Seller();
        ReflectionTestUtils.setField(s, "id", id);
        s.setName("Test Seller");
        s.setContactInfo("contact@test.com");
        s.setRegistrationDate(LocalDateTime.of(2026, 1, 1, 0, 0));
        return s;
    }

    private Transaction transaction(Long id, Seller seller, BigDecimal amount, PaymentType type) {
        Transaction t = new Transaction();
        ReflectionTestUtils.setField(t, "id", id);
        t.setSeller(seller);
        t.setAmount(amount);
        t.setPaymentType(type);
        t.setTransactionDate(LocalDateTime.of(2026, 3, 15, 12, 0));
        return t;
    }

    @Test
    void getAllTransaction_returnsMappedDtos() {
        Seller s = seller(1L);
        Pageable pageable = PageRequest.of(0, 20);
        when(transactionRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(
                transaction(1L, s, BigDecimal.valueOf(100), PaymentType.CASH),
                transaction(2L, s, BigDecimal.valueOf(200), PaymentType.CARD)
        )));

        Page<TransactionDto> result = transactionService.getAllTransaction(pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).amount()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(result.getContent().get(0).paymentType()).isEqualTo(PaymentType.CASH);
        assertThat(result.getContent().get(1).amount()).isEqualByComparingTo(BigDecimal.valueOf(200));
        assertThat(result.getContent().get(1).paymentType()).isEqualTo(PaymentType.CARD);
    }

    @Test
    void getAllTransaction_emptyRepository_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 20);
        when(transactionRepository.findAll(pageable)).thenReturn(Page.empty());

        assertThat(transactionService.getAllTransaction(pageable).getContent()).isEmpty();
    }

    @Test
    void getTransactionById_found_returnsFullDto() {
        Seller s = seller(1L);
        Transaction t = transaction(1L, s, BigDecimal.valueOf(500), PaymentType.TRANSFER);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(t));

        TransactionDto result = transactionService.getTransactionById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.sellerId()).isEqualTo(1L);
        assertThat(result.amount()).isEqualByComparingTo(BigDecimal.valueOf(500));
        assertThat(result.paymentType()).isEqualTo(PaymentType.TRANSFER);
        assertThat(result.transactionDate()).isEqualTo(LocalDateTime.of(2026, 3, 15, 12, 0));
    }

    @Test
    void getTransactionById_notFound_throwsWithIdInMessage() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransactionById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createTransaction_sellerExists_savesAndReturnsDto() {
        Seller s = seller(1L);
        CreateTransactionDto dto = new CreateTransactionDto(1L, BigDecimal.valueOf(300), PaymentType.CARD);
        Transaction saved = transaction(1L, s, BigDecimal.valueOf(300), PaymentType.CARD);
        when(sellerRepository.findById(1L)).thenReturn(Optional.of(s));
        when(transactionRepository.save(any())).thenReturn(saved);

        LocalDateTime before = LocalDateTime.now();
        TransactionDto result = transactionService.createTransaction(dto);
        LocalDateTime after = LocalDateTime.now();

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        assertThat(captor.getValue().getSeller()).isEqualTo(s);
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(300));
        assertThat(captor.getValue().getPaymentType()).isEqualTo(PaymentType.CARD);
        assertThat(captor.getValue().getTransactionDate()).isBetween(before, after);
        assertThat(result.sellerId()).isEqualTo(1L);
        assertThat(result.amount()).isEqualByComparingTo(BigDecimal.valueOf(300));
        assertThat(result.paymentType()).isEqualTo(PaymentType.CARD);
    }

    @Test
    void createTransaction_sellerNotFound_throwsWithIdInMessage() {
        when(sellerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.createTransaction(
                new CreateTransactionDto(99L, BigDecimal.valueOf(100), PaymentType.CASH)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void getAllTransactionsBySellerId_sellerExists_returnsMappedDtos() {
        Seller s = seller(1L);
        when(sellerRepository.existsById(1L)).thenReturn(true);
        when(transactionRepository.findAllBySellerId(1L)).thenReturn(List.of(
                transaction(1L, s, BigDecimal.valueOf(100), PaymentType.CASH)
        ));

        List<TransactionDto> result = transactionService.getAllTransactionsBySellerId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).sellerId()).isEqualTo(1L);
    }

    @Test
    void getAllTransactionsBySellerId_sellerWithNoTransactions_returnsEmptyList() {
        when(sellerRepository.existsById(1L)).thenReturn(true);
        when(transactionRepository.findAllBySellerId(1L)).thenReturn(List.of());

        assertThat(transactionService.getAllTransactionsBySellerId(1L)).isEmpty();
    }

    @Test
    void getAllTransactionsBySellerId_sellerNotFound_throws() {
        when(sellerRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> transactionService.getAllTransactionsBySellerId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(transactionRepository, never()).findAllBySellerId(any());
    }

}