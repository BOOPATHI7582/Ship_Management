package com.company.exportplatform.repository;

import com.company.exportplatform.entity.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

    Optional<OtpCode> findFirstByUserIdAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc(Long userId, String purpose);

    void deleteByUserIdAndPurposeAndUsedAtIsNull(Long userId, String purpose);
}