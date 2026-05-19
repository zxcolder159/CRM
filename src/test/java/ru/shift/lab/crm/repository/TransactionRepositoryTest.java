package ru.shift.lab.crm.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import ru.shift.lab.crm.entity.Seller;
import ru.shift.lab.crm.entity.Transaction;
import ru.shift.lab.crm.util.PaymentType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TransactionRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private TransactionRepository transactionRepository;

    private static final LocalDateTime START = LocalDateTime.of(2026, 1, 1, 0, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 1, 31, 23, 59, 59);

    private Seller persistSeller(String name, boolean deleted) {
        Seller s = new Seller();
        s.setName(name);
        s.setContactInfo("contact@test.com");
        s.setRegistrationDate(LocalDateTime.of(2025, 1, 1, 0, 0));
        s.setDeleted(deleted);
        return em.persistAndFlush(s);
    }

    private Transaction addTransaction(Seller seller, BigDecimal amount, LocalDateTime date) {
        Transaction t = new Transaction();
        t.setSeller(seller);
        t.setAmount(amount);
        t.setPaymentType(PaymentType.CASH);
        t.setTransactionDate(date);
        return em.persistAndFlush(t);
    }

    @Test
    void findAllBySellerId_returnsOnlySellerTransactions() {
        Seller alice = persistSeller("Alice", false);
        Seller bob = persistSeller("Bob", false);
        addTransaction(alice, BigDecimal.valueOf(100), START);
        addTransaction(alice, BigDecimal.valueOf(200), START.plusDays(1));
        addTransaction(bob, BigDecimal.valueOf(300), START);

        Page<Transaction> result = transactionRepository.findAllBySellerId(alice.getId(), Pageable.unpaged());

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(t -> t.getSeller().getId().equals(alice.getId()));
    }

    @Test
    void findAllBySellerId_noTransactions_returnsEmpty() {
        Seller alice = persistSeller("Alice", false);

        Page<Transaction> result = transactionRepository.findAllBySellerId(alice.getId(), Pageable.unpaged());

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findTopSellerId_returnsSellerWithHighestSum() {
        Seller alice = persistSeller("Alice", false);
        Seller bob = persistSeller("Bob", false);
        addTransaction(alice, BigDecimal.valueOf(500), START);
        addTransaction(bob, BigDecimal.valueOf(2000), START);

        Optional<Long> result = transactionRepository.findTopSellerId(START, END);

        assertThat(result).contains(bob.getId());
    }

    @Test
    void findTopSellerId_noTransactionsInPeriod_returnsEmpty() {
        Seller alice = persistSeller("Alice", false);
        addTransaction(alice, BigDecimal.valueOf(500), LocalDateTime.of(2025, 6, 1, 0, 0));

        Optional<Long> result = transactionRepository.findTopSellerId(START, END);

        assertThat(result).isEmpty();
    }

    @Test
    void findTopSellerId_deletedSeller_isExcluded() {
        Seller active = persistSeller("Active", false);
        Seller deleted = persistSeller("Deleted", true);
        addTransaction(active, BigDecimal.valueOf(100), START);
        addTransaction(deleted, BigDecimal.valueOf(9999), START);

        Optional<Long> result = transactionRepository.findTopSellerId(START, END);

        assertThat(result).contains(active.getId());
    }

    @Test
    void findTopSellerId_transactionsOutsidePeriod_notCounted() {
        Seller alice = persistSeller("Alice", false);
        Seller bob = persistSeller("Bob", false);
        addTransaction(alice, BigDecimal.valueOf(100), START);
        addTransaction(bob, BigDecimal.valueOf(9999), LocalDateTime.of(2025, 12, 31, 23, 59));

        Optional<Long> result = transactionRepository.findTopSellerId(START, END);

        assertThat(result).contains(alice.getId());
    }

    @Test
    void findBestDay_returnsDayWithMostTransactions() {
        Seller alice = persistSeller("Alice", false);
        LocalDateTime jan5 = LocalDateTime.of(2026, 1, 5, 10, 0);
        LocalDateTime jan6 = LocalDateTime.of(2026, 1, 6, 10, 0);
        addTransaction(alice, BigDecimal.ONE, jan5);
        addTransaction(alice, BigDecimal.ONE, jan6);
        addTransaction(alice, BigDecimal.ONE, jan6);

        Optional<LocalDate> result = transactionRepository.findBestDay(alice.getId());

        assertThat(result).contains(LocalDate.of(2026, 1, 6));
    }

    @Test
    void findBestDay_noTransactions_returnsEmpty() {
        Seller alice = persistSeller("Alice", false);

        Optional<LocalDate> result = transactionRepository.findBestDay(alice.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findBestWeek_returnsWeekStartWithMostTransactions() {
        Seller alice = persistSeller("Alice", false);
        LocalDateTime week2 = LocalDateTime.of(2026, 1, 7, 10, 0);
        LocalDateTime week3a = LocalDateTime.of(2026, 1, 12, 10, 0);
        LocalDateTime week3b = LocalDateTime.of(2026, 1, 13, 10, 0);
        addTransaction(alice, BigDecimal.ONE, week2);
        addTransaction(alice, BigDecimal.ONE, week3a);
        addTransaction(alice, BigDecimal.ONE, week3b);

        Optional<LocalDate> result = transactionRepository.findBestWeek(alice.getId());

        assertThat(result).contains(LocalDate.of(2026, 1, 12));
    }

    @Test
    void findBestWeek_noTransactions_returnsEmpty() {
        Seller alice = persistSeller("Alice", false);

        Optional<LocalDate> result = transactionRepository.findBestWeek(alice.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findBestMonth_returnsMonthStartWithMostTransactions() {
        Seller alice = persistSeller("Alice", false);
        addTransaction(alice, BigDecimal.ONE, LocalDateTime.of(2026, 1, 5, 10, 0));
        addTransaction(alice, BigDecimal.ONE, LocalDateTime.of(2026, 2, 5, 10, 0));
        addTransaction(alice, BigDecimal.ONE, LocalDateTime.of(2026, 2, 10, 10, 0));

        Optional<LocalDate> result = transactionRepository.findBestMonth(alice.getId());

        assertThat(result).contains(LocalDate.of(2026, 2, 1));
    }

    @Test
    void findBestMonth_noTransactions_returnsEmpty() {
        Seller alice = persistSeller("Alice", false);

        Optional<LocalDate> result = transactionRepository.findBestMonth(alice.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findBestQuarter_returnsQuarterStartWithMostTransactions() {
        Seller alice = persistSeller("Alice", false);
        addTransaction(alice, BigDecimal.ONE, LocalDateTime.of(2026, 1, 10, 10, 0));
        addTransaction(alice, BigDecimal.ONE, LocalDateTime.of(2026, 4, 5, 10, 0));
        addTransaction(alice, BigDecimal.ONE, LocalDateTime.of(2026, 5, 5, 10, 0));

        Optional<LocalDate> result = transactionRepository.findBestQuarter(alice.getId());

        assertThat(result).contains(LocalDate.of(2026, 4, 1));
    }

    @Test
    void findBestQuarter_noTransactions_returnsEmpty() {
        Seller alice = persistSeller("Alice", false);

        Optional<LocalDate> result = transactionRepository.findBestQuarter(alice.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findBestYear_returnsYearStartWithMostTransactions() {
        Seller alice = persistSeller("Alice", false);
        addTransaction(alice, BigDecimal.ONE, LocalDateTime.of(2025, 6, 1, 10, 0));
        addTransaction(alice, BigDecimal.ONE, LocalDateTime.of(2026, 1, 1, 10, 0));
        addTransaction(alice, BigDecimal.ONE, LocalDateTime.of(2026, 6, 1, 10, 0));

        Optional<LocalDate> result = transactionRepository.findBestYear(alice.getId());

        assertThat(result).contains(LocalDate.of(2026, 1, 1));
    }

    @Test
    void findBestYear_noTransactions_returnsEmpty() {
        Seller alice = persistSeller("Alice", false);

        Optional<LocalDate> result = transactionRepository.findBestYear(alice.getId());

        assertThat(result).isEmpty();
    }
}