package com.company.exportplatform.service;

import com.company.exportplatform.entity.TaxRate;
import com.company.exportplatform.entity.enums.TaxTreatment;
import com.company.exportplatform.entity.enums.TaxType;
import com.company.exportplatform.exception.BadRequestException;
import com.company.exportplatform.repository.TaxRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

/**
 * Treatment-aware tax computation. Never assumes a rate: explicit treatment
 * and/or an explicit tax rate row drives the math, EXEMPT/ZERO_RATED stay at 0,
 * and missing configuration yields zero tax instead of a wrong guess.
 */
@Service
@RequiredArgsConstructor
public class TaxCalculationService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final TaxRateRepository taxRateRepository;

    public record TaxResult(
            BigDecimal taxableAmount,
            BigDecimal totalTax,
            BigDecimal cgstAmount,
            BigDecimal sgstAmount,
            BigDecimal igstAmount,
            TaxTreatment treatment,
            String rateName,
            BigDecimal ratePercent
    ) {
        public static TaxResult untaxed(BigDecimal taxableAmount, TaxTreatment treatment) {
            return new TaxResult(scale(taxableAmount), BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, treatment, null, null);
        }
    }

    @Transactional(readOnly = true)
    public TaxResult compute(String treatmentName, Long taxRateId, String country, BigDecimal taxableAmount) {
        BigDecimal taxable = taxableAmount == null ? BigDecimal.ZERO : taxableAmount;
        TaxTreatment treatment = normalize(treatmentName);

        if (taxable.signum() <= 0 || treatment == null
                || treatment == TaxTreatment.EXEMPT || treatment == TaxTreatment.ZERO_RATED) {
            return TaxResult.untaxed(taxable, treatment == null ? TaxTreatment.EXEMPT : treatment);
        }
        if (treatment == TaxTreatment.CUSTOM && taxRateId == null) {
            return TaxResult.untaxed(taxable, TaxTreatment.CUSTOM);
        }

        if (taxRateId != null) {
            TaxRate rate = taxRateRepository.findById(taxRateId)
                    .filter(TaxRate::isActive)
                    .orElseThrow(() -> new BadRequestException("Selected tax rate is inactive or missing"));
            return applySingleRate(taxable,
                    treatment != null ? treatment : fromTaxType(rate.getTaxType()), rate);
        }

        if (treatment == TaxTreatment.CGST_SGST) {
            TaxRate cgst = resolve(TaxType.CGST, country);
            TaxRate sgst = resolve(TaxType.SGST, country);
            if (cgst == null || sgst == null) {
                return TaxResult.untaxed(taxable, TaxTreatment.EXEMPT);
            }
            BigDecimal cgstAmount = percentOf(taxable, cgst.getRate());
            BigDecimal sgstAmount = percentOf(taxable, sgst.getRate());
            return new TaxResult(scale(taxable),
                    scale(cgstAmount.add(sgstAmount)), scale(cgstAmount), scale(sgstAmount),
                    BigDecimal.ZERO, TaxTreatment.CGST_SGST,
                    cgst.getName() + " + " + sgst.getName(),
                    scale(cgst.getRate().add(sgst.getRate())));
        }

        if (treatment == TaxTreatment.IGST) {
            TaxRate igst = resolve(TaxType.IGST, country);
            if (igst == null) {
                return TaxResult.untaxed(taxable, TaxTreatment.IGST);
            }
            return applySingleRate(taxable, TaxTreatment.IGST, igst);
        }

        return TaxResult.untaxed(taxable, treatment);
    }

    /** Latest active configured rate of the given type for the given country (or any jurisdiction). */
    @Transactional(readOnly = true)
    public TaxRate resolve(TaxType type, String country) {
        TaxRate exact = country != null && !country.isBlank()
                ? taxRateRepository.findFirstByTaxTypeAndCountryAndActiveTrueOrderByEffectiveFromDesc(type, country)
                .orElse(null)
                : null;
        if (exact != null) {
            return exact;
        }
        return taxRateRepository.findByActiveTrue().stream()
                .filter(r -> r.getTaxType() == type)
                .filter(r -> country == null || country.isBlank() || isExportContext(country, r.getCountry()))
                .findFirst()
                .orElse(null);
    }

    private TaxResult applySingleRate(BigDecimal taxable, TaxTreatment treatment, TaxRate rate) {
        BigDecimal tax = percentOf(taxable, rate.getRate());
        BigDecimal cgst = BigDecimal.ZERO;
        BigDecimal sgst = BigDecimal.ZERO;
        BigDecimal igst = BigDecimal.ZERO;
        switch (rate.getTaxType()) {
            case CGST -> cgst = tax;
            case SGST -> sgst = tax;
            case IGST -> igst = tax;
            default -> { /* CUSTOM flat rate lands in total only */ }
        }
        return new TaxResult(scale(taxable), tax, scale(cgst), scale(sgst), scale(igst),
                treatment, rate.getName(), scale(rate.getRate()));
    }

    private TaxTreatment normalize(String treatmentName) {
        if (treatmentName == null || treatmentName.isBlank()) {
            return null;
        }
        return Arrays.stream(TaxTreatment.values())
                .filter(t -> t.name().equalsIgnoreCase(treatmentName.trim()))
                .findFirst()
                .orElseThrow(() -> new BadRequestException(
                        "Unknown tax treatment. Allowed: CGST_SGST, IGST, EXEMPT, ZERO_RATED, CUSTOM"));
    }

    private TaxTreatment fromTaxType(TaxType type) {
        return switch (type) {
            case CGST, SGST -> TaxTreatment.CGST_SGST;
            case IGST -> TaxTreatment.IGST;
            case EXEMPT -> TaxTreatment.EXEMPT;
            case ZERO_RATED -> TaxTreatment.ZERO_RATED;
            case CUSTOM -> TaxTreatment.CUSTOM;
        };
    }

    private boolean isExportContext(String billingCountry, String rateCountry) {
        // Rates seeded against "India" also serve exports billed outside India when no local rate exists.
        return !"India".equalsIgnoreCase(billingCountry);
    }

    private static BigDecimal percentOf(BigDecimal amount, BigDecimal rate) {
        return amount.multiply(rate).divide(HUNDRED, 4, RoundingMode.HALF_UP);
    }

    private static BigDecimal scale(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.setScale(4, RoundingMode.HALF_UP);
    }
}
