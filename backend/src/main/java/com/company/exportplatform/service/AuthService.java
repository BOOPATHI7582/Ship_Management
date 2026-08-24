package com.company.exportplatform.service;

import com.company.exportplatform.dto.request.ForgotPasswordRequest;
import com.company.exportplatform.dto.request.LoginRequest;
import com.company.exportplatform.dto.request.RegisterRequest;
import com.company.exportplatform.dto.request.ResetPasswordRequest;
import com.company.exportplatform.dto.response.ApiResponse;
import com.company.exportplatform.dto.response.AuthResponse;
import com.company.exportplatform.entity.Client;
import com.company.exportplatform.entity.PasswordReset;
import com.company.exportplatform.entity.User;
import com.company.exportplatform.entity.enums.RoleName;
import com.company.exportplatform.exception.BadRequestException;
import com.company.exportplatform.repository.ClientRepository;
import com.company.exportplatform.repository.PasswordResetRepository;
import com.company.exportplatform.repository.RoleRepository;
import com.company.exportplatform.repository.UserRepository;
import com.company.exportplatform.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final int RESET_TOKEN_BYTES = 32;
    private static final String RESET_EMAIL_SUBJECT = "ExportPlatform - Password reset request";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ClientRepository clientRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final com.company.exportplatform.repository.EmailVerificationRepository emailVerificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final MailService mailService;
    private final com.company.exportplatform.security.LoginLockoutService loginLockoutService;
    private final AuditService auditService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.password-reset.expiration-minutes:60}")
    private long resetExpirationMinutes;

    @Value("${app.email-verification.expiration-hours:24}")
    private long verificationExpirationHours;

    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional
    public com.company.exportplatform.dto.response.RegisterResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("An account with this email already exists");
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName().trim());
        user.setCompanyName(trimOrNull(request.getCompanyName()));
        user.setPhone(trimOrNull(request.getPhone()));
        user.setCountry(trimOrNull(request.getCountry()));
        // accounts stay inactive until the email address is verified
        user.setActive(false);
        user.setRole(roleRepository.findByName(RoleName.CLIENT)
                .orElseThrow(() -> new IllegalStateException("CLIENT role is missing from seed data")));
        user = userRepository.save(user);

        Client clientProfile = new Client();
        clientProfile.setUser(user);
        clientProfile.setCountry(user.getCountry());
        clientRepository.save(clientProfile);

        String devLink = issueEmailVerification(user);

        log.info("Registered new {} #{} ({}) - awaiting email verification",
                RoleName.CLIENT, user.getId(), email);

        return new com.company.exportplatform.dto.response.RegisterResponse(
                email, true, devLink);
    }

    /**
     * Creates a single-use verification token and emails it. Returns the raw
     * link only when mail delivery is disabled (local development convenience).
     */
    private String issueEmailVerification(User user) {
        emailVerificationRepository.deleteByUserIdAndVerifiedAtIsNull(user.getId());

        String rawToken = newToken();
        com.company.exportplatform.entity.EmailVerification verification =
                new com.company.exportplatform.entity.EmailVerification();
        verification.setUser(user);
        verification.setTokenHash(hashToken(rawToken));
        verification.setExpiresAt(LocalDateTime.now().plusHours(verificationExpirationHours));
        emailVerificationRepository.save(verification);

        String link = baseUrl + "/verify-email?token=" + rawToken;
        mailService.sendHtml(user.getEmail(), VERIFY_EMAIL_SUBJECT,
                buildVerificationEmail(user, link, rawToken));
        log.info("Issued email verification token for user #{}", user.getId());
        // Raw link is only exposed in the API response when SMTP is not
        // configured (local dev convenience). Never leak it in production.
        return mailService.isMailEnabled() ? null : link;
    }

    @Transactional
    public com.company.exportplatform.dto.response.VerifyEmailResponse verifyEmail(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new BadRequestException("This verification link is invalid");
        }
        com.company.exportplatform.entity.EmailVerification verification =
                emailVerificationRepository.findByTokenHash(hashToken(rawToken))
                        .orElseThrow(() -> new BadRequestException("This verification link is invalid"));

        if (verification.getVerifiedAt() != null) {
            return new com.company.exportplatform.dto.response.VerifyEmailResponse(
                    verification.getUser().getEmail(), true);
        }
        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException(
                    "This verification link has expired. Please request a new one from the login page.");
        }

        verification.setVerifiedAt(LocalDateTime.now());
        emailVerificationRepository.save(verification);

        User user = verification.getUser();
        user.setActive(true);
        userRepository.save(user);
        auditService.record(user.getEmail(), "EMAIL_VERIFIED", "USER", user.getId(), null, null);
        log.info("User #{} verified their email address", user.getId());

        return new com.company.exportplatform.dto.response.VerifyEmailResponse(user.getEmail(), true);
    }

    @Transactional
    public ApiResponse<Void> resendVerification(String email) {
        String normalized = email == null ? "" : email.trim().toLowerCase();
        userRepository.findByEmail(normalized)
                .filter(user -> !user.isActive())
                .ifPresent(user -> issueEmailVerification(user));
        return ApiResponse.ok("If an unverified account exists for this email, "
                + "a new verification link has been sent.");
    }

    private static final String VERIFY_EMAIL_SUBJECT = "ExportPlatform - Verify your email";

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        loginLockoutService.checkAllowed(email);
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    email, request.getPassword()));
        } catch (AuthenticationException ex) {
            // DisabledException covers unverified/deactivated accounts; tell
            // genuinely unverified users what to do without leaking anything else.
            boolean pendingVerification = userRepository.findByEmail(email)
                    .filter(u -> !u.isActive())
                    .map(u -> emailVerificationRepository
                            .findFirstByUserIdAndVerifiedAtIsNullOrderByCreatedAtDesc(u.getId())
                            .map(v -> v.getExpiresAt().isAfter(LocalDateTime.now()))
                            .orElse(false))
                    .orElse(false);
            if (pendingVerification) {
                throw new BadRequestException(
                        "Please verify your email before logging in. Check your inbox for the verification link.");
            }
            loginLockoutService.recordFailure(email);
            auditService.record(email, "LOGIN_FAILED", "USER", null, null, null);
            throw new BadRequestException("Invalid email or password");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));
        if (!user.isActive()) {
            boolean pendingVerification = emailVerificationRepository
                    .findFirstByUserIdAndVerifiedAtIsNullOrderByCreatedAtDesc(user.getId())
                    .map(v -> v.getExpiresAt().isAfter(LocalDateTime.now()))
                    .orElse(false);
            throw new BadRequestException(pendingVerification
                    ? "Please verify your email before logging in. Check your inbox for the verification link."
                    : "This account has been deactivated");
        }
        loginLockoutService.recordSuccess(email);
        user.setLastLoginAt(LocalDateTime.now());
        auditService.record(email, "LOGIN_SUCCESS", "USER", user.getId(), null, null);

        return issueToken(user);
    }

    /**
     * Always answers the same generic message so attackers cannot probe
     * which emails exist. Creates a single-use hashed token when it does.
     */
    @Transactional
    public ApiResponse<Void> forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        userRepository.findByEmail(email).ifPresent(user -> {
            passwordResetRepository.deleteByUserIdAndUsedAtIsNull(user.getId());

            String rawToken = newToken();
            PasswordReset reset = new PasswordReset();
            reset.setUser(user);
            reset.setTokenHash(hashToken(rawToken));
            reset.setExpiresAt(LocalDateTime.now().plusMinutes(resetExpirationMinutes));
            passwordResetRepository.save(reset);

            String link = baseUrl + "/reset-password?token=" + rawToken;
            mailService.sendHtml(user.getEmail(), RESET_EMAIL_SUBJECT, buildResetEmail(user, link, rawToken));
            log.info("Issued password reset token for user #{}", user.getId());
        });
        return ApiResponse.ok("If an account exists for this email, a reset link has been sent.");
    }

    @Transactional
    public ApiResponse<Void> resetPassword(ResetPasswordRequest request) {
        PasswordReset reset = passwordResetRepository.findByTokenHash(hashToken(request.getToken()))
                .orElseThrow(() -> new BadRequestException("This reset link is invalid"));

        if (reset.getUsedAt() != null) {
            throw new BadRequestException("This reset link has already been used");
        }
        if (reset.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("This reset link has expired. Please request a new one.");
        }

        User user = reset.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        reset.setUsedAt(LocalDateTime.now());
        // Invalidate any other outstanding tokens for safety.
        passwordResetRepository.deleteByUserIdAndUsedAtIsNull(user.getId());

        log.info("Password reset completed for user #{}", user.getId());
        return ApiResponse.ok("Your password has been updated. You can now log in.");
    }

    @Transactional(readOnly = true)
    public com.company.exportplatform.dto.response.UserResponse currentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new com.company.exportplatform.exception.ResourceNotFoundException("User not found"));
        return com.company.exportplatform.dto.response.UserResponse.from(user);
    }

    private AuthResponse issueToken(User user) {
        return AuthResponse.of(jwtService.generateToken(user), jwtService.getExpirationMs(), user);
    }

    private String newToken() {
        byte[] bytes = new byte[RESET_TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    private String trimOrNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private String buildVerificationEmail(User user, String link, String rawToken) {
        return """
                <html><body style="font-family:Arial,sans-serif;color:#1f2937;">
                  <h2>Welcome, %s!</h2>
                  <p>Confirm your email address to activate your ExportPlatform account.</p>
                  <p><a href="%s" style="background:#0ea5e9;color:#ffffff;padding:10px 18px;border-radius:6px;text-decoration:none;">Verify my email</a></p>
                  <p>Or paste this code on the verification page (valid for %d hours):</p>
                  <p><code>%s</code></p>
                  <p>If you did not create this account, you can safely ignore this email.</p>
                </body></html>
                """.formatted(escapeHtml(user.getFullName()), escapeHtml(link),
                verificationExpirationHours, rawToken);
    }

    private String buildResetEmail(User user, String link, String rawToken) {
        return """
                <html><body style="font-family:Arial,sans-serif;color:#1f2937;">
                  <h2>Hello %s,</h2>
                  <p>We received a request to reset your ExportPlatform password.</p>
                  <p><a href="%s" style="background:#2563eb;color:#ffffff;padding:10px 18px;border-radius:6px;text-decoration:none;">Reset your password</a></p>
                  <p>Or paste this code into the reset form (valid for %d minutes):</p>
                  <p><code>%s</code></p>
                  <p>If you did not request this, you can safely ignore this email.</p>
                </body></html>
                """.formatted(escapeHtml(user.getFullName()), escapeHtml(link),
                resetExpirationMinutes, rawToken);
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }
}
