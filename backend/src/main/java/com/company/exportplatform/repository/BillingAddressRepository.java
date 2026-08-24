package com.company.exportplatform.repository;

import com.company.exportplatform.entity.BillingAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillingAddressRepository extends JpaRepository<BillingAddress, Long> {

    List<BillingAddress> findByClientId(Long clientId);
}
