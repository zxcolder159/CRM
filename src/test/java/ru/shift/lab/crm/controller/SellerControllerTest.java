package ru.shift.lab.crm.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.shift.lab.crm.dto.BestPeriodResultDto;
import ru.shift.lab.crm.dto.CreateSellerDto;
import ru.shift.lab.crm.dto.SellerDto;
import ru.shift.lab.crm.dto.UpdateSellerDto;
import ru.shift.lab.crm.exception.ResourceNotFoundException;
import ru.shift.lab.crm.service.SellerService;
import ru.shift.lab.crm.util.PeriodType;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SellerController.class)
class SellerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SellerService sellerService;

    private SellerDto sellerDto(Long id, String name) {
        return new SellerDto(id, name, "contact@test.com", LocalDateTime.of(2026, 1, 1, 0, 0));
    }

    private SellerDto sellerDto(Long id, String name, String contactInfo) {
        return new SellerDto(id, name, contactInfo, LocalDateTime.of(2026, 1, 1, 0, 0));
    }

    @Test
    void getAllSellers_returns200WithList() throws Exception {
        when(sellerService.getAllSellers()).thenReturn(List.of(sellerDto(1L, "Alice"), sellerDto(2L, "Bob")));

        mockMvc.perform(get("/api/sellers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[1].name").value("Bob"));
    }

    @Test
    void getAllSellers_emptyList_returns200WithEmptyArray() throws Exception {
        when(sellerService.getAllSellers()).thenReturn(List.of());

        mockMvc.perform(get("/api/sellers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getSellerById_found_returns200WithDto() throws Exception {
        when(sellerService.getSellerById(1L)).thenReturn(sellerDto(1L, "Alice"));

        mockMvc.perform(get("/api/sellers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.contactInfo").value("contact@test.com"));
    }

    @Test
    void getSellerById_notFound_returns404WithStatus() throws Exception {
        when(sellerService.getSellerById(99L))
                .thenThrow(new ResourceNotFoundException("Продавец с id 99 не найден"));

        mockMvc.perform(get("/api/sellers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Продавец с id 99 не найден"));
    }

    @Test
    void updateSeller_found_returns200WithUpdatedDto() throws Exception {
        when(sellerService.updateSeller(eq(1L), any())).thenReturn(sellerDto(1L, "New Name", "new@test.com"));

        mockMvc.perform(put("/api/sellers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateSellerDto("New Name", "new@test.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.contactInfo").value("new@test.com"));
    }

    @Test
    void createSeller_validBody_returns201() throws Exception {
        CreateSellerDto request = new CreateSellerDto("Alice", "alice@test.com");
        when(sellerService.createSeller(any())).thenReturn(sellerDto(1L, "Alice"));

        mockMvc.perform(post("/api/sellers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Alice"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void createSeller_invalidName_returns400(String invalidName) throws Exception {
        mockMvc.perform(post("/api/sellers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateSellerDto(invalidName, "alice@test.com"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createSeller_emptyContactInfo_returns400() throws Exception {
        mockMvc.perform(post("/api/sellers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateSellerDto("Alice", ""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createSeller_invalidJson_returns400() throws Exception {
        mockMvc.perform(post("/api/sellers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not json at all"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateSeller_notFound_returns404() throws Exception {
        when(sellerService.updateSeller(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Продавец с id 99 не найден"));

        mockMvc.perform(put("/api/sellers/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateSellerDto("Name", "contact"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Продавец с id 99 не найден"));
    }

    @Test
    void updateSeller_emptyName_returns400() throws Exception {
        mockMvc.perform(put("/api/sellers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateSellerDto("", "contact"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteSeller_found_returns204() throws Exception {
        mockMvc.perform(delete("/api/sellers/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteSeller_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Продавец с id 99 не найден"))
                .when(sellerService).deleteSeller(99L);

        mockMvc.perform(delete("/api/sellers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Продавец с id 99 не найден"));
    }

    @Test
    void getMostProductive_validParams_returns200() throws Exception {
        when(sellerService.getMostProductiveSeller(any(), eq(PeriodType.MONTH)))
                .thenReturn(sellerDto(1L, "Alice"));

        mockMvc.perform(get("/api/sellers/most-productive")
                        .param("startDate", "2026-01-01T00:00:00")
                        .param("periodType", "MONTH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    void getMostProductive_missingStartDate_returns400() throws Exception {
        mockMvc.perform(get("/api/sellers/most-productive")
                        .param("periodType", "MONTH"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void getMostProductive_noTransactions_returns404() throws Exception {
        when(sellerService.getMostProductiveSeller(any(), any()))
                .thenThrow(new ResourceNotFoundException("Нет транзакций за указанный период"));

        mockMvc.perform(get("/api/sellers/most-productive")
                        .param("startDate", "2026-01-01T00:00:00")
                        .param("periodType", "DAY"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Нет транзакций за указанный период"));
    }

    @Test
    void getUnderperforming_returns200WithList() throws Exception {
        when(sellerService.getUnderperformingSellers(any(), any(), any()))
                .thenReturn(List.of(sellerDto(1L, "Alice")));

        mockMvc.perform(get("/api/sellers/underperforming")
                        .param("startDate", "2026-01-01T00:00:00")
                        .param("periodType", "MONTH")
                        .param("threshold", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Alice"));
    }

    @Test
    void getUnderperforming_noResults_returns200WithEmptyList() throws Exception {
        when(sellerService.getUnderperformingSellers(any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/sellers/underperforming")
                        .param("startDate", "2026-01-01T00:00:00")
                        .param("periodType", "YEAR")
                        .param("threshold", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getBestPeriod_found_returns200WithRange() throws Exception {
        BestPeriodResultDto dto = new BestPeriodResultDto(
                1L,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31),
                PeriodType.MONTH);
        when(sellerService.getBestSalesPeriod(eq(1L), eq(PeriodType.MONTH))).thenReturn(dto);

        mockMvc.perform(get("/api/sellers/1/best-period")
                        .param("periodType", "MONTH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sellerId").value(1))
                .andExpect(jsonPath("$.periodType").value("MONTH"))
                .andExpect(jsonPath("$.periodStart").value("2026-01-01"))
                .andExpect(jsonPath("$.periodEnd").value("2026-01-31"));
    }

    @Test
    void getBestPeriod_sellerNotFound_returns404() throws Exception {
        when(sellerService.getBestSalesPeriod(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Продавец с id 99 не найден"));

        mockMvc.perform(get("/api/sellers/99/best-period")
                        .param("periodType", "DAY"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Продавец с id 99 не найден"));
    }
}