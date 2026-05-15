package ru.shift.lab.crm.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.shift.lab.crm.dto.CreateSellerDto;
import ru.shift.lab.crm.dto.SellerDto;
import ru.shift.lab.crm.dto.UpdateSellerDto;
import ru.shift.lab.crm.service.SellerService;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** REST контроллер для управления продавцами. */
@RestController
@RequestMapping("/api/sellers")
@RequiredArgsConstructor
public class SellerController {
    private final SellerService sellerService;

    /** Получить список всех продавцов. */
    @GetMapping
    public List<SellerDto> getAllSellers() {
        return sellerService.getAllSellers();
    }

    /** Получить самого продуктивного продавца за указанный период. */
    @GetMapping("/most-productive")
    public SellerDto getMostProductive(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                           LocalDateTime start,
                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                       LocalDateTime end) {
        return sellerService.getMostProductiveSeller(start, end);
    }

    /** Получить список продавцов с суммой транзакций ниже указанного порога за период. */
    @GetMapping("/underperforming")
    public List<SellerDto> getUnderperformingSellers(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                        LocalDateTime start,
                                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                    LocalDateTime end,
                                                    @RequestParam BigDecimal threshold) {
        return sellerService.getUnderperformingSellers(start, end, threshold);
    }

    /** Создать нового продавца. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SellerDto createSeller(@Valid @RequestBody CreateSellerDto createSellerDto) {
        return sellerService.createSeller(createSellerDto);
    }

    /** Получить информацию о продавце по ID. */
    @GetMapping("/{id}")
    public SellerDto getSellerById(@PathVariable Long id) {
        return sellerService.getSellerById(id);
    }

    /** Обновить информацию о продавце. */
    @PutMapping("/{id}")
    public SellerDto updateSeller(@PathVariable Long id, @Valid @RequestBody UpdateSellerDto updateSellerDto) {
        return sellerService.updateSeller(id, updateSellerDto);
    }

    /** Удалить продавца. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSeller(@PathVariable Long id) {
        sellerService.deleteSeller(id);
    }
}
