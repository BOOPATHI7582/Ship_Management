package com.company.exportplatform.service;

import com.company.exportplatform.entity.OtpCode;
import com.company.exportplatform.entity.User;
import com.company.exportplatform.exception.BadRequestException;
import com.company.exportplatform.repository.OtpCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;

/**
 * Issues and verifies short-lived 6-digit one-time passwords. Codes are
 * stored hashed (single-use) with a bounded number of wrong attempts.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OtpService {

    public static final String PURPOSE_LOGIN = "LOGIN";

    private static final int MAX_ATTEMPTS = 5;

    private final OtpCodeRepository otpCodeRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Creates a fresh code for a user/purpose, invalidating any previous
     * unused one. Returns the raw code so the caller can email it. Only the
     * hash is persisted.
     */
    @Transactional
    public String issue(User user, String purpose, int ttlMinutes) {
        otpCodeRepository.deleteByUserIdAndPurposeAndUsedAtIsNull(user.getId(), purpose);

        String code = sixDigits();
        OtpCode otp = new OtpCode();
        otp.setUser(user);
        otp.setPurpose(purpose);
        otp.setCodeHash(hash(code));
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(ttlMinutes));
        otpCodeRepository.save(otp);
        return code;
    }

    /**
     * Verifies the supplied code for a user/purpose.
     *
     * @throws BadRequestException with a user-facing message on any failure
     */
    @Transactional
    public void verify(User user, String purpose, String rawCode) {
        if (rawCode == null || rawCode.isBlank()) {
            throw new BadRequestException("Please enter the verification code");
        }
        OtpCode otp = otpCodeRepository
                .findFirstByUserIdAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc(user.getId(), purpose)
                .orElseThrow(() -> new BadRequestException(
                        "This verification code is invalid or has already been used. Please request a new one."));
        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            otpCodeRepository.delete(otp);
            throw new BadRequestException("This verification code has expired. Please request a new one.");
        }
        if (otp.getAttempts() >= MAX_ATTEMPTS) {
            otpCodeRepository.delete(otp);
            throw new BadRequestException("Too many failed attempts. Please request a new verification code.");
        }
        if (!otp.getCodeHash().equals(hash(rawCode.trim()))) {
            otp.setAttempts(otp.getAttempts() + 1);
            otpCodeRepository.save(otp);
            throw new BadRequestException("Incorrect verification code. Please try again.");
        }
        otp.setUsedAt(LocalDateTime.now());
        otpCodeRepository.save(otp);
        log.info("OTP verified for user #{} purpose {}", user.getId(), purpose);
    }

    private String sixDigits() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

    private String hash(String rawCode) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawCode.trim().getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}