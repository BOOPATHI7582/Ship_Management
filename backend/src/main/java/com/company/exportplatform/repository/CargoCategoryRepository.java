package com.company.exportplatform.repository;

import com.company.exportplatform.entity.CargoCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CargoCategoryRepository extends JpaRepository<CargoCategory, Long> {

    Optional<CargoCategory> findByName(String name);

    boolean existsByName(String name);

    List<CargoCategory> findByActiveTrue();
}
