package ru.shift.lab.crm.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import ru.shift.lab.crm.entity.Seller;
import ru.shift.lab.crm.entity.Transaction;
import ru.shift.lab.crm.util.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class SellerRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private SellerRepository sellerRepository;

    private static final LocalDateTime START = LocalDateTime.of(2026, 1, 1, 0, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 1, 31, 23, 59, 59);
    private static final BigDecimal THRESHOLD = BigDecimal.valueOf(1000);

    private Seller persistSeller(String name) {
        Seller s = new Seller();
        s.setName(name);
        s.setContactInfo("contact@test.com");
        s.setRegistrationDate(LocalDateTime.of(2025, 1, 1, 0, 0));
        s.setDeleted(false);
        return em.persist(s);
    }

    private void addTransaction(Seller seller, BigDecimal amount, LocalDateTime date) {
        Transaction t = new Transaction();
        t.setSeller(seller);
        t.setAmount(amount);
        t.setPaymentType(PaymentType.CASH);
        t.setTransactionDate(date);
        em.persist(t);
    }

    @Test
    void findUnderperformingSellers_sellerBelowThreshold_isReturned() {
        Seller seller = persistSeller("Alice");
        addTransaction(seller, BigDecimal.valueOf(500), START.plusDays(1));
        em.flush();

        List<Seller> result = sellerRepository.findUnderperformingSellers(START, END, THRESHOLD);

        assertThat(result).extracting(Seller::getName).containsExactly("Alice");
    }

    @Test
    void findUnderperformingSellers_sellerAboveThreshold_isNotReturned() {
        Seller seller = persistSeller("Bob");
        addTransaction(seller, BigDecimal.valueOf(2000), START.plusDays(1));
        em.flush();

        List<Seller> result = sellerRepository.findUnderperformingSellers(START, END, THRESHOLD);

        assertThat(result).isEmpty();
    }

    @Test
    void findUnderperformingSellers_sellerWithNoTransactions_isReturned() {
        persistSeller("Charlie");
        em.flush();

        List<Seller> result = sellerRepository.findUnderperformingSellers(START, END, THRESHOLD);

        assertThat(result).extracting(Seller::getName).containsExactly("Charlie");
    }

    @Test
    void findUnderperformingSellers_sellerWithExactThreshold_isNotReturned() {
        Seller seller = persistSeller("Dave");
        addTransaction(seller, THRESHOLD, START.plusDays(1));
        em.flush();

        List<Seller> result = sellerRepository.findUnderperformingSellers(START, END, THRESHOLD);

        assertThat(result).isEmpty();
    }

    @Test
    void findUnderperformingSellers_deletedSeller_isNotReturned() {
        Seller seller = persistSeller("Eve");
        seller.setDeleted(true);
        em.flush();

        List<Seller> result = sellerRepository.findUnderperformingSellers(START, END, THRESHOLD);

        assertThat(result).isEmpty();
    }

    @Test
    void findUnderperformingSellers_transactionsOutsidePeriod_notCounted() {
        Seller seller = persistSeller("Frank");
        addTransaction(seller, BigDecimal.valueOf(2000), LocalDateTime.of(2025, 12, 31, 23, 59));
        em.flush();

        List<Seller> result = sellerRepository.findUnderperformingSellers(START, END, THRESHOLD);

        assertThat(result).extracting(Seller::getName).containsExactly("Frank");
    }

    @Test
    void findUnderperformingSellers_mixedSellers_returnsOnlyUnderperforming() {
        Seller low = persistSeller("Low");
        Seller high = persistSeller("High");
        addTransaction(low, BigDecimal.valueOf(100), START.plusDays(1));
        addTransaction(high, BigDecimal.valueOf(5000), START.plusDays(1));
        em.flush();

        List<Seller> result = sellerRepository.findUnderperformingSellers(START, END, THRESHOLD);

        assertThat(result).extracting(Seller::getName).containsExactly("Low");
    }
}