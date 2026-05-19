package ru.shift.lab.crm.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.shift.lab.crm.dto.CreateTransactionDto;
import ru.shift.lab.crm.dto.TransactionDto;
import ru.shift.lab.crm.exception.ResourceNotFoundException;
import ru.shift.lab.crm.service.TransactionService;
import ru.shift.lab.crm.util.PaymentType;
import tools.jackson.databind.ObjectMapper;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransactionService transactionService;

    private TransactionDto transactionDto(Long id, Long sellerId, BigDecimal amount, PaymentType type) {
        return new TransactionDto(id, sellerId, amount, type, LocalDateTime.of(2026, 1, 15, 12, 0));
    }

    @Test
    void getAllTransactions_returns200WithPage() throws Exception {
        when(transactionService.getAllTransaction(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(
                transactionDto(1L, 1L, BigDecimal.valueOf(100), PaymentType.CASH),
                transactionDto(2L, 2L, BigDecimal.valueOf(200), PaymentType.CARD)
        )));

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].amount").value(100))
                .andExpect(jsonPath("$.content[0].paymentType").value("CASH"))
                .andExpect(jsonPath("$.content[1].amount").value(200))
                .andExpect(jsonPath("$.content[1].paymentType").value("CARD"));
    }

    @Test
    void getAllTransactions_emptyList_returns200WithEmptyPage() throws Exception {
        when(transactionService.getAllTransaction(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void getTransactionById_found_returns200WithDto() throws Exception {
        when(transactionService.getTransactionById(1L))
                .thenReturn(transactionDto(1L, 1L, BigDecimal.valueOf(500), PaymentType.TRANSFER));

        mockMvc.perform(get("/api/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(500))
                .andExpect(jsonPath("$.paymentType").value("TRANSFER"))
                .andExpect(jsonPath("$.sellerId").value(1));
    }

    @Test
    void getTransactionById_notFound_returns404WithStatus() throws Exception {
        when(transactionService.getTransactionById(99L))
                .thenThrow(new ResourceNotFoundException("Транзакции с id 99 не найдено"));

        mockMvc.perform(get("/api/transactions/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Транзакции с id 99 не найдено"));
    }

    @Test
    void createTransaction_validBody_returns201() throws Exception {
        CreateTransactionDto request = new CreateTransactionDto(1L, BigDecimal.valueOf(300), PaymentType.CARD);

        when(transactionService.createTransaction(any()))
                .thenReturn(transactionDto(1L, 1L, BigDecimal.valueOf(300), PaymentType.CARD));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.paymentType").value("CARD"));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidTransactionDtos")
    void createTransaction_invalidBody_returns400(CreateTransactionDto invalidRequest) throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    private static Stream<Arguments> provideInvalidTransactionDtos() {
        return Stream.of(
                Arguments.of(new CreateTransactionDto(1L, BigDecimal.valueOf(-100), PaymentType.CASH)), // negative amount
                Arguments.of(new CreateTransactionDto(1L, BigDecimal.ZERO, PaymentType.CASH)),          // zero amount
                Arguments.of(new CreateTransactionDto(null, BigDecimal.valueOf(100), PaymentType.CASH)),// null sellerId
                Arguments.of(new CreateTransactionDto(1L, BigDecimal.valueOf(100), null))               // null paymentType
        );
    }

    @Test
    void createTransaction_invalidPaymentType_returns400() throws Exception {
        Map<String, Object> request = Map.of(
                "sellerId", 1,
                "amount", 100,
                "paymentType", "BITCOIN"
        );

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTransaction_invalidJson_returns400() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not valid json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTransaction_sellerNotFound_returns404() throws Exception {
        CreateTransactionDto request = new CreateTransactionDto(99L, BigDecimal.valueOf(100), PaymentType.CASH);

        when(transactionService.createTransaction(any()))
                .thenThrow(new ResourceNotFoundException("Продавец с id 99 не найден"));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Продавец с id 99 не найден"));
    }

    @Test
    void getTransactionsBySeller_found_returns200WithPage() throws Exception {
        when(transactionService.getAllTransactionsBySellerId(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(
                        transactionDto(1L, 1L, BigDecimal.valueOf(100), PaymentType.CASH)
                )));

        mockMvc.perform(get("/api/transactions/seller/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].sellerId").value(1));
    }

    @Test
    void getTransactionsBySeller_sellerWithNoTransactions_returns200EmptyPage() throws Exception {
        when(transactionService.getAllTransactionsBySellerId(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/transactions/seller/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void getTransactionsBySeller_sellerNotFound_returns404() throws Exception {
        when(transactionService.getAllTransactionsBySellerId(eq(99L), any(Pageable.class)))
                .thenThrow(new ResourceNotFoundException("Продавец с id 99 не найден"));

        mockMvc.perform(get("/api/transactions/seller/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Продавец с id 99 не найден"));
    }
}