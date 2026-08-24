package com.company.exportplatform.repository;

import com.company.exportplatform.entity.Role;
import com.company.exportplatform.entity.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
}
