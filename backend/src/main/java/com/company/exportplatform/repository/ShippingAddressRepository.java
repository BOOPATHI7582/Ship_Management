package com.company.exportplatform.repository;

import com.company.exportplatform.entity.ShippingAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShippingAddressRepository extends JpaRepository<ShippingAddress, Long> {

    List<ShippingAddress> findByClientId(Long clientId);
}
