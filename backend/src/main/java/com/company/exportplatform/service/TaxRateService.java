package com.company.exportplatform.service;

import com.company.exportplatform.dto.request.TaxRateRequest;
import com.company.exportplatform.dto.response.TaxRateResponse;
import com.company.exportplatform.entity.TaxRate;
import com.company.exportplatform.entity.enums.TaxType;
import com.company.exportplatform.exception.BadRequestException;
import com.company.exportplatform.exception.ResourceNotFoundException;
import com.company.exportplatform.repository.TaxRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Admin-managed tax master data. Documents snapshot rates at issue time,
 * so editing or removing a rate never rewrites historical paperwork.
 */
@Service
@RequiredArgsConstructor
public class TaxRateService {

    private final TaxRateRepository taxRateRepository;

    @Transactional(readOnly = true)
    public Page<TaxRateResponse> list(String taxType, String country, Boolean active, Pageable pageable) {
        Specification<TaxRate> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (taxType != null && !taxType.isBlank()) {
                try {
                    predicates.add(cb.equal(root.get("taxType"), TaxType.valueOf(taxType)));
                } catch (IllegalArgumentException ex) {
                    throw new BadRequestException("Invalid tax type");
                }
            }
            if (country != null && !country.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("country")), country.trim().toLowerCase()));
            }
            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        return taxRateRepository.findAll(spec, pageable).map(TaxRateService::toResponse);
    }

    @Transactional(readOnly = true)
    public List<TaxRateResponse> listActive() {
        return taxRateRepository.findByActiveTrue().stream().map(TaxRateService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TaxRateResponse get(Long id) {
        return toResponse(findTaxRate(id));
    }

    @Transactional
    public TaxRateResponse create(TaxRateRequest request) {
        validate(request);
        TaxRate rate = new TaxRate();
        apply(rate, request);
        return toResponse(taxRateRepository.save(rate));
    }

    @Transactional
    public TaxRateResponse update(Long id, TaxRateRequest request) {
        validate(request);
        TaxRate rate = findTaxRate(id);
        apply(rate, request);
        return toResponse(taxRateRepository.save(rate));
    }

    @Transactional
    public TaxRateResponse toggleActive(Long id, boolean active) {
        TaxRate rate = findTaxRate(id);
        rate.setActive(active);
        return toResponse(taxRateRepository.save(rate));
    }

    @Transactional
    public void delete(Long id) {
        taxRateRepository.delete(findTaxRate(id));
    }

    private void validate(TaxRateRequest request) {
        if (request.rate() == null || request.rate().compareTo(BigDecimal.ZERO) < 0
                || request.rate().compareTo(new BigDecimal("100")) > 0) {
            throw new BadRequestException("Tax rate must be between 0 and 100 percent");
        }
        if ((request.taxType() == TaxType.CGST || request.taxType() == TaxType.SGST)
                && request.rate().compareTo(new BigDecimal("50")) > 0) {
            throw new BadRequestException("CGST/SGST components cannot exceed 50 percent");
        }
    }

    private void apply(TaxRate rate, TaxRateRequest request) {
        rate.setName(request.name().trim());
        rate.setTaxType(request.taxType());
        rate.setRate(request.rate());
        rate.setCountry(request.country().trim());
        rate.setJurisdiction(request.jurisdiction() == null || request.jurisdiction().isBlank()
                ? null : request.jurisdiction().trim());
        rate.setEffectiveFrom(request.effectiveFrom());
        rate.setActive(request.active() == null || request.active());
        rate.setDescription(request.description() == null || request.description().isBlank()
                ? null : request.description().trim());
    }

    private TaxRate findTaxRate(Long id) {
        return taxRateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tax rate not found"));
    }

    static TaxRateResponse toResponse(TaxRate rate) {
        return new TaxRateResponse(
                rate.getId(), rate.getName(), rate.getTaxType().name(), rate.getRate(),
                rate.getCountry(), rate.getJurisdiction(), rate.getEffectiveFrom(),
                rate.isActive(), rate.getDescription(), rate.getCreatedAt());
    }
}
