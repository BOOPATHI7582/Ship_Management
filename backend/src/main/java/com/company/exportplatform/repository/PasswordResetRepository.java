package com.company.exportplatform.repository;

import com.company.exportplatform.entity.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetRepository extends JpaRepository<PasswordReset, Long> {

    Optional<PasswordReset> findByTokenHash(String tokenHash);

    void deleteByUserIdAndUsedAtIsNull(Long userId);
}
