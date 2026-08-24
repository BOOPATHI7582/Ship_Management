package com.company.exportplatform.service;

import com.company.exportplatform.entity.TaxRate;
import com.company.exportplatform.entity.enums.TaxTreatment;
import com.company.exportplatform.entity.enums.TaxType;
import com.company.exportplatform.exception.BadRequestException;
import com.company.exportplatform.repository.TaxRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaxCalculationServiceTest {

    @Mock
    private TaxRateRepository taxRateRepository;

    private TaxCalculationService service;

    private TaxRate rate(TaxType type, String name, String percent, String country) {
        TaxRate r = new TaxRate();
        r.setTaxType(type);
        r.setName(name);
        r.setRate(new BigDecimal(percent));
        r.setCountry(country);
        r.setActive(true);
        return r;
    }

    @BeforeEach
    void setUp() {
        service = new TaxCalculationService(taxRateRepository);
        lenient().when(taxRateRepository.findFirstByTaxTypeAndCountryAndActiveTrueOrderByEffectiveFromDesc(
                        any(), any())).thenReturn(Optional.empty());
        lenient().when(taxRateRepository.findByActiveTrue()).thenReturn(List.of());
    }

    @Test
    @DisplayName("zero or negative taxable amounts produce zero tax")
    void zeroTaxableIsUntaxed() {
        var result = service.compute("IGST", null, "Germany", BigDecimal.ZERO);
        assertThat(result.totalTax()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.taxableAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("EXEMPT treatment never touches the repository")
    void exemptStaysAtZero() {
        var result = service.compute("EXEMPT", 5L, "India", new BigDecimal("10000"));
        assertThat(result.treatment()).isEqualTo(TaxTreatment.EXEMPT);
        assertThat(result.totalTax()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.rateName()).isNull();
    }

    @Test
    @DisplayName("CUSTOM treatment without a rate row stays untaxed instead of guessing")
    void customWithoutRateIsUntaxed() {
        var result = service.compute("CUSTOM", null, "India", new BigDecimal("5000"));
        assertThat(result.totalTax()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("explicit active rate id applies that single rate (CUSTOM treatment)")
    void explicitRateApplies() {
        when(taxRateRepository.findById(7L))
                .thenReturn(Optional.of(rate(TaxType.IGST, "IGST 18%", "18.00", null)));

        var result = service.compute("CUSTOM", 7L, "Germany", new BigDecimal("10000"));

        assertThat(result.totalTax()).isEqualByComparingTo(new BigDecimal("1800.0000"));
        assertThat(result.igstAmount()).isEqualByComparingTo(new BigDecimal("1800.0000"));
        assertThat(result.cgstAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.rateName()).isEqualTo("IGST 18%");
    }

    @Test
    @DisplayName("inactive or missing explicit rate id is rejected")
    void inactiveRateIsRejected() {
        TaxRate inactive = rate(TaxType.IGST, "Old IGST", "18", null);
        inactive.setActive(false);
        when(taxRateRepository.findById(9L)).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> service.compute("CUSTOM", 9L, "Germany", new BigDecimal("10000")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("inactive");
    }

    @Test
    @DisplayName("CGST_SGST splits the combined rate into two halves")
    void cgstSgstSplits() {
        when(taxRateRepository.findByActiveTrue()).thenReturn(List.of(
                rate(TaxType.CGST, "CGST 9%", "9.00", "India"),
                rate(TaxType.SGST, "SGST 9%", "9.00", "India")));

        var result = service.compute("CGST_SGST", null, "Germany", new BigDecimal("10000"));

        assertThat(result.cgstAmount()).isEqualByComparingTo(new BigDecimal("900.0"));
        assertThat(result.sgstAmount()).isEqualByComparingTo(new BigDecimal("900.0"));
        assertThat(result.igstAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.totalTax()).isEqualByComparingTo(new BigDecimal("1800.0"));
        assertThat(result.ratePercent()).isEqualByComparingTo(new BigDecimal("18.00"));
    }

    @Test
    @DisplayName("IGST treatment with no configured rate falls back to untaxed")
    void missingIgstFallsBack() {
        var result = service.compute("IGST", null, "Mars", new BigDecimal("10000"));
        assertThat(result.totalTax()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.treatment()).isEqualTo(TaxTreatment.IGST);
    }

    @Test
    @DisplayName("resolve prefers an exact country match over generic rates")
    void resolvePrefersExactCountry() {
        TaxRate exact = rate(TaxType.IGST, "Export IGST", "0", "Germany");
        when(taxRateRepository.findFirstByTaxTypeAndCountryAndActiveTrueOrderByEffectiveFromDesc(
                eq(TaxType.IGST), eq("Germany"))).thenReturn(Optional.of(exact));

        TaxRate found = service.resolve(TaxType.IGST, "Germany");
        assertThat(found.getName()).isEqualTo("Export IGST");
    }
}
