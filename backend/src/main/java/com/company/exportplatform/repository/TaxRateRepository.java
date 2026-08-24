package com.company.exportplatform.repository;

import com.company.exportplatform.entity.TaxRate;
import com.company.exportplatform.entity.enums.TaxType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface TaxRateRepository
        extends JpaRepository<TaxRate, Long>, JpaSpecificationExecutor<TaxRate> {

    List<TaxRate> findByActiveTrue();

    List<TaxRate> findByCountryAndActiveTrue(String country);

    List<TaxRate> findByTaxType(TaxType taxType);

    Optional<TaxRate> findFirstByTaxTypeAndCountryAndActiveTrueOrderByEffectiveFromDesc(TaxType taxType, String country);
}
