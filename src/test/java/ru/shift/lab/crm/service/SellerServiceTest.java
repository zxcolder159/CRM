package ru.shift.lab.crm.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import ru.shift.lab.crm.dto.BestPeriodResultDto;
import ru.shift.lab.crm.dto.CreateSellerDto;
import ru.shift.lab.crm.dto.SellerDto;
import ru.shift.lab.crm.dto.UpdateSellerDto;
import ru.shift.lab.crm.entity.Seller;
import ru.shift.lab.crm.exception.ResourceNotFoundException;
import ru.shift.lab.crm.repository.SellerRepository;
import ru.shift.lab.crm.repository.TransactionRepository;
import ru.shift.lab.crm.util.PeriodType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SellerServiceTest {

    @Mock
    private SellerRepository sellerRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private SellerService sellerService;

    private Seller seller(Long id, String name) {
        Seller s = new Seller();
        ReflectionTestUtils.setField(s, "id", id);
        s.setName(name);
        s.setContactInfo("contact@test.com");
        s.setRegistrationDate(LocalDateTime.of(2026, 1, 1, 0, 0));
        return s;
    }

    @Test
    void getAllSellers_returnsMappedDtos() {
        Pageable pageable = PageRequest.of(0, 20);
        when(sellerRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(seller(1L, "Alice"), seller(2L, "Bob"))));

        Page<SellerDto> result = sellerService.getAllSellers(pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).name()).isEqualTo("Alice");
        assertThat(result.getContent().get(1).name()).isEqualTo("Bob");
    }

    @Test
    void getAllSellers_emptyRepository_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 20);
        when(sellerRepository.findAll(pageable)).thenReturn(Page.empty());

        assertThat(sellerService.getAllSellers(pageable).getContent()).isEmpty();
    }

    @Test
    void getSellerById_found_returnsFullDto() {
        Seller s = seller(1L, "Alice");
        when(sellerRepository.findById(1L)).thenReturn(Optional.of(s));

        SellerDto result = sellerService.getSellerById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Alice");
        assertThat(result.contactInfo()).isEqualTo("contact@test.com");
        assertThat(result.registrationDate()).isEqualTo(LocalDateTime.of(2026, 1, 1, 0, 0));
    }

    @Test
    void getSellerById_notFound_throwsWithIdInMessage() {
        when(sellerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sellerService.getSellerById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createSeller_setsRegistrationDateToNowAndSaves() {
        CreateSellerDto dto = new CreateSellerDto("Alice", "alice@test.com");
        Seller saved = seller(1L, "Alice");
        when(sellerRepository.save(any(Seller.class))).thenReturn(saved);

        LocalDateTime before = LocalDateTime.now();
        SellerDto result = sellerService.createSeller(dto);
        LocalDateTime after = LocalDateTime.now();

        ArgumentCaptor<Seller> captor = ArgumentCaptor.forClass(Seller.class);
        verify(sellerRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Alice");
        assertThat(captor.getValue().getContactInfo()).isEqualTo("alice@test.com");
        assertThat(captor.getValue().getRegistrationDate()).isBetween(before, after);
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void updateSeller_found_updatesNameAndContactInfo() {
        Seller s = seller(1L, "Old Name");
        when(sellerRepository.findById(1L)).thenReturn(Optional.of(s));

        SellerDto result = sellerService.updateSeller(1L, new UpdateSellerDto("New Name", "new@test.com"));

        assertThat(result.name()).isEqualTo("New Name");
        assertThat(result.contactInfo()).isEqualTo("new@test.com");

        ArgumentCaptor<Seller> captor = ArgumentCaptor.forClass(Seller.class);
        verify(sellerRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("New Name");
        assertThat(captor.getValue().getContactInfo()).isEqualTo("new@test.com");
    }

    @Test
    void updateSeller_notFound_throws() {
        when(sellerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sellerService.updateSeller(99L, new UpdateSellerDto("Name", "contact")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteSeller_found_setsIsDeletedTrue() {
        Seller s = seller(1L, "Alice");
        when(sellerRepository.findById(1L)).thenReturn(Optional.of(s));

        sellerService.deleteSeller(1L);

        assertThat(s.isDeleted()).isTrue();
        verify(sellerRepository, never()).delete(any());
    }

    @Test
    void deleteSeller_notFound_throws() {
        when(sellerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sellerService.deleteSeller(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @ParameterizedTest
    @MethodSource("providePeriodsForMostProductive")
    void getMostProductiveSeller_returnsDtoAndVerifiesEndDate(PeriodType periodType, LocalDate expectedStartDate, LocalDate expectedEndDate) {
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        when(transactionRepository.findTopSellerId(any(), any())).thenReturn(Optional.of(1L));
        lenient().when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller(1L, "Alice")));

        SellerDto result = sellerService.getMostProductiveSeller(start, periodType);

        ArgumentCaptor<LocalDateTime> startCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> endCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(transactionRepository).findTopSellerId(startCaptor.capture(), endCaptor.capture());
        assertThat(startCaptor.getValue().toLocalDate()).isEqualTo(expectedStartDate);
        assertThat(startCaptor.getValue().toLocalTime()).isEqualTo(LocalTime.MIDNIGHT);
        assertThat(endCaptor.getValue().toLocalDate()).isEqualTo(expectedEndDate);
        assertThat(endCaptor.getValue().toLocalTime()).isEqualTo(LocalTime.MAX);
        
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Alice");
    }

    private static Stream<Arguments> providePeriodsForMostProductive() {
        return Stream.of(
                Arguments.of(PeriodType.DAY, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 1)),
                Arguments.of(PeriodType.WEEK, LocalDate.of(2025, 12, 29), LocalDate.of(2026, 1, 4)),
                Arguments.of(PeriodType.MONTH, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31)),
                Arguments.of(PeriodType.QUARTER, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31)),
                Arguments.of(PeriodType.YEAR, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31))
        );
    }

    @Test
    void getMostProductiveSeller_noTransactions_throws() {
        when(transactionRepository.findTopSellerId(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sellerService.getMostProductiveSeller(LocalDateTime.of(2026, 3, 15, 10, 0), PeriodType.DAY))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getMostProductiveSeller_sellerRemovedBetweenQueries_throws() {
        when(transactionRepository.findTopSellerId(any(), any())).thenReturn(Optional.of(1L));
        when(sellerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sellerService.getMostProductiveSeller(LocalDateTime.of(2026, 3, 15, 10, 0), PeriodType.DAY))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getUnderperformingSellers_returnsMappedList() {
        when(sellerRepository.findUnderperformingSellers(any(), any(), any()))
                .thenReturn(List.of(seller(1L, "Alice")));

        List<SellerDto> result = sellerService.getUnderperformingSellers(
                LocalDateTime.of(2026, 1, 1, 0, 0), PeriodType.MONTH, BigDecimal.valueOf(1000));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Alice");
    }

    @Test
    void getUnderperformingSellers_noResults_returnsEmptyList() {
        when(sellerRepository.findUnderperformingSellers(any(), any(), any())).thenReturn(List.of());

        assertThat(sellerService.getUnderperformingSellers(
                LocalDateTime.of(2026, 1, 1, 0, 0), PeriodType.MONTH, BigDecimal.valueOf(1000))).isEmpty();
    }

    @Test
    void getUnderperformingSellers_passesCorrectPeriodBoundariesToRepository() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        when(sellerRepository.findUnderperformingSellers(any(), any(), any())).thenReturn(List.of());

        sellerService.getUnderperformingSellers(start, PeriodType.QUARTER, BigDecimal.valueOf(500));

        ArgumentCaptor<LocalDateTime> endCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(sellerRepository).findUnderperformingSellers(eq(start), endCaptor.capture(), eq(BigDecimal.valueOf(500)));
        assertThat(endCaptor.getValue().toLocalDate()).isEqualTo(LocalDate.of(2026, 3, 31));
    }

    @Test
    void getBestSalesPeriod_sellerNotFound_throws() {
        when(sellerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sellerService.getBestSalesPeriod(99L, PeriodType.DAY))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @ParameterizedTest
    @MethodSource("providePeriodsForBestSales")
    void getBestSalesPeriod_returnsCorrectEndRange(PeriodType periodType, LocalDate periodStart, LocalDate expectedEndDate) {
        lenient().when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller(1L, "Alice")));

        switch (periodType) {
            case DAY -> lenient().when(transactionRepository.findBestDay(1L)).thenReturn(Optional.of(periodStart));
            case WEEK -> lenient().when(transactionRepository.findBestWeek(1L)).thenReturn(Optional.of(periodStart));
            case MONTH -> lenient().when(transactionRepository.findBestMonth(1L)).thenReturn(Optional.of(periodStart));
            case QUARTER -> lenient().when(transactionRepository.findBestQuarter(1L)).thenReturn(Optional.of(periodStart));
            case YEAR -> lenient().when(transactionRepository.findBestYear(1L)).thenReturn(Optional.of(periodStart));
        }

        BestPeriodResultDto result = sellerService.getBestSalesPeriod(1L, periodType);

        assertThat(result.periodStart()).isEqualTo(periodStart);
        assertThat(result.periodEnd()).isEqualTo(expectedEndDate);
        assertThat(result.sellerId()).isEqualTo(1L);
        assertThat(result.periodType()).isEqualTo(periodType);
    }

    private static Stream<Arguments> providePeriodsForBestSales() {
        return Stream.of(
                Arguments.of(PeriodType.DAY, LocalDate.of(2026, 3, 15), LocalDate.of(2026, 3, 15)),
                Arguments.of(PeriodType.WEEK, LocalDate.of(2026, 3, 9), LocalDate.of(2026, 3, 15)),
                Arguments.of(PeriodType.MONTH, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31)),
                Arguments.of(PeriodType.MONTH, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 29)),
                Arguments.of(PeriodType.QUARTER, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31)),
                Arguments.of(PeriodType.YEAR, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31))
        );
    }

    @Test
    void getBestSalesPeriod_noTransactions_throws() {
        when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller(1L, "Alice")));
        when(transactionRepository.findBestDay(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sellerService.getBestSalesPeriod(1L, PeriodType.DAY))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}