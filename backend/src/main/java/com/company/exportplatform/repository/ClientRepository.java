package com.company.exportplatform.repository;

import com.company.exportplatform.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long>, JpaSpecificationExecutor<Client> {

    Optional<Client> findByUserId(Long userId);

    @Query("select c from Client c join fetch c.user u order by lower(coalesce(u.companyName, u.fullName))")
    List<Client> findAllWithUser();
}
