package com.company.exportplatform.controller;

import com.company.exportplatform.dto.request.TaxRateRequest;
import com.company.exportplatform.dto.response.ApiResponse;
import com.company.exportplatform.dto.response.TaxRateResponse;
import com.company.exportplatform.service.TaxRateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tax-rates")
@RequiredArgsConstructor
public class AdminTaxRateController {

    private final TaxRateService taxRateService;

    @GetMapping
    public ApiResponse<Page<TaxRateResponse>> list(
            @RequestParam(required = false) String taxType,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<TaxRateResponse> result = taxRateService.list(taxType, country, active,
                PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "effectiveFrom")));
        return ApiResponse.ok("Tax rates", result);
    }

    @GetMapping("/active")
    public ApiResponse<List<TaxRateResponse>> activeRates() {
        return ApiResponse.ok("Active tax rates", taxRateService.listActive());
    }

    @GetMapping("/{id}")
    public ApiResponse<TaxRateResponse> get(@PathVariable Long id) {
        return ApiResponse.ok("Tax rate", taxRateService.get(id));
    }

    @PostMapping
    public ApiResponse<TaxRateResponse> create(@Valid @RequestBody TaxRateRequest request) {
        return ApiResponse.ok("Tax rate created", taxRateService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<TaxRateResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody TaxRateRequest request) {
        return ApiResponse.ok("Tax rate updated", taxRateService.update(id, request));
    }

    @PutMapping("/{id}/toggle")
    public ApiResponse<TaxRateResponse> toggle(@PathVariable Long id,
                                               @RequestParam boolean active) {
        return ApiResponse.ok(active ? "Tax rate enabled" : "Tax rate disabled",
                taxRateService.toggleActive(id, active));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        taxRateService.delete(id);
        return ApiResponse.ok("Tax rate deleted", null);
    }
}
