package com.company.exportplatform.repository;

import com.company.exportplatform.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByRoleName(com.company.exportplatform.entity.enums.RoleName roleName, Pageable pageable);

    long countByRoleName(com.company.exportplatform.entity.enums.RoleName roleName);
}
