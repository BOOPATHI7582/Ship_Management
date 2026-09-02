package com.company.exportplatform.service;

import com.company.exportplatform.dto.request.ForgotPasswordRequest;
import com.company.exportplatform.dto.request.GoogleLoginRequest;
import com.company.exportplatform.dto.request.LoginOtpRequest;
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
    private static final String LOGIN_OTP_SUBJECT = "ExportPlatform - Your login verification code";
    private static final int EMAIL_VERIFY_MAX_ATTEMPTS = 5;

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
    private final OtpService otpService;
    private final GoogleLoginService googleLoginService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.password-reset.expiration-minutes:60}")
    private long resetExpirationMinutes;

    @Value("${app.email-verification.expiration-hours:24}")
    private long verificationExpirationHours;

    @Value("${app.otp.login-ttl-minutes:10}")
    private int loginOtpTtlMinutes;

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
     * Creates a single-use 6-digit OTP and emails it. Returns the raw link
     * only when mail delivery is disabled (local development convenience).
     */
    private String issueEmailVerification(User user) {
        emailVerificationRepository.deleteByUserIdAndVerifiedAtIsNull(user.getId());

        String rawToken = newOtpCode();
        com.company.exportplatform.entity.EmailVerification verification =
                new com.company.exportplatform.entity.EmailVerification();
        verification.setUser(user);
        verification.setTokenHash(hashToken(rawToken));
        verification.setExpiresAt(LocalDateTime.now().plusHours(verificationExpirationHours));
        emailVerificationRepository.save(verification);

        String link = baseUrl + "/verify-email?token=" + rawToken;
        mailService.sendHtml(user.getEmail(), VERIFY_EMAIL_SUBJECT,
                buildVerificationEmail(user, link, rawToken));
        log.info("Issued email verification OTP for user #{}", user.getId());
        // Raw link is only exposed in the API response when SMTP is not
        // configured (local dev convenience). Never leak it in production.
        return mailService.isMailEnabled() ? null : link;
    }

    @Transactional
    public AuthResponse verifyEmail(
            String rawToken, String email) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new BadRequestException("This verification code is invalid");
        }
        String token = rawToken.trim();

        // The token is the source of truth. The email is only a best-effort
        // hint for attempt limiting when the token itself cannot be resolved,
        // so a valid code works even if the caller mistyped their email.
        com.company.exportplatform.entity.EmailVerification verification =
                emailVerificationRepository.findByTokenHash(hashToken(token))
                        .orElse(null);
        if (verification == null && email != null && !email.isBlank()) {
            verification = findByPendingEmail(email.trim().toLowerCase(), token);
        }
        if (verification == null) {
            throw new BadRequestException("This verification code is invalid");
        }
        return completeVerification(verification);
    }

    /**
     * Locates a single pending verification for an account so wrong-attempt
     * limits can be enforced when the exact code hash is unknown.
     *
     * @return the pending verification, or null when no usable record exists.
     */
    private com.company.exportplatform.entity.EmailVerification findByPendingEmail(
            String email, String token) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return null;
        }
        com.company.exportplatform.entity.EmailVerification verification =
                emailVerificationRepository
                        .findFirstByUserIdAndVerifiedAtIsNullOrderByCreatedAtDesc(user.getId())
                        .orElse(null);
        if (verification == null || verification.getAttempts() >= EMAIL_VERIFY_MAX_ATTEMPTS) {
            if (verification != null) {
                emailVerificationRepository.delete(verification);
            }
            throw new BadRequestException("Too many failed attempts. Please request a new verification code.");
        }
        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            emailVerificationRepository.delete(verification);
            throw new BadRequestException("This verification code has expired. Please request a new one.");
        }
        if (!verification.getTokenHash().equals(hashToken(token))) {
            verification.setAttempts(verification.getAttempts() + 1);
            emailVerificationRepository.save(verification);
            throw new BadRequestException("Incorrect verification code. Please try again.");
        }
        return verification;
    }

    private AuthResponse completeVerification(
            com.company.exportplatform.entity.EmailVerification verification) {
        if (verification.getVerifiedAt() != null) {
            return issueToken(verification.getUser());
        }
        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException(
                    "This verification code has expired. Please request a new one from the login page.");
        }

        User user = verification.getUser();
        verification.setVerifiedAt(LocalDateTime.now());
        emailVerificationRepository.save(verification);

        user.setActive(true);
        userRepository.save(user);
        user.setLastLoginAt(LocalDateTime.now());
        auditService.record(user.getEmail(), "EMAIL_VERIFIED", "USER", user.getId(), null, null);
        log.info("User #{} verified their email address", user.getId());

        return issueToken(user);
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
        if (user.getPasswordHash() == null) {
            throw new BadRequestException(
                    "This account uses Google sign-in. Please log in with your Google account.");
        }

        // Credentials are correct: start a session and return the token
        // immediately (no second OTP step for sign-in).
        loginLockoutService.recordSuccess(email);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        auditService.record(email, "LOGIN_SUCCESS", "USER", user.getId(), null, null);
        return issueToken(user);
    }

    @Transactional
    public AuthResponse verifyLoginOtp(LoginOtpRequest request) {
        String email = request.email().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .filter(User::isActive)
                .orElseThrow(() -> new BadRequestException("Invalid verification code"));
        otpService.verify(user, OtpService.PURPOSE_LOGIN, request.otp());
        user.setLastLoginAt(LocalDateTime.now());
        auditService.record(email, "LOGIN_SUCCESS", "USER", user.getId(), null, null);
        loginLockoutService.recordSuccess(email);
        return issueToken(user);
    }

    @Transactional
    public ApiResponse<Void> resendLoginOtp(String email) {
        String normalized = email == null ? "" : email.trim().toLowerCase();
        userRepository.findByEmail(normalized)
                .filter(User::isActive)
                .filter(user -> user.getPasswordHash() != null)
                .ifPresent(user -> {
                    String code = otpService.issue(user, OtpService.PURPOSE_LOGIN, loginOtpTtlMinutes);
                    mailService.sendHtml(normalized, LOGIN_OTP_SUBJECT, buildLoginOtpEmail(user, code));
                });
        return ApiResponse.ok("If your account is ready, a new verification code has been sent.");
    }

    @Transactional
    public AuthResponse googleLogin(GoogleLoginRequest request) {
        GoogleLoginService.GoogleProfile profile = googleLoginService.verify(request.idToken());
        String email = profile.email().toLowerCase();

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setGoogleSub(profile.sub());
            user.setFullName(profile.name() != null && !profile.name().isBlank()
                    ? profile.name().trim() : "Google User");
            user.setActive(true);
            user.setRole(roleRepository.findByName(RoleName.CLIENT)
                    .orElseThrow(() -> new IllegalStateException("CLIENT role is missing from seed data")));
            user = userRepository.save(user);

            Client clientProfile = new Client();
            clientProfile.setUser(user);
            clientRepository.save(clientProfile);
            log.info("Created Google sign-in account for {}", email);
        } else {
            if (user.getGoogleSub() == null) {
                user.setGoogleSub(profile.sub());
            }
            user.setActive(true);
        }
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        auditService.record(email, "GOOGLE_LOGIN", "USER", user.getId(), null, null);
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

    private String newOtpCode() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
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
                <html><body style="font-family:Arial,sans-serif;background:#f8fafc;padding:24px;">
                  <div style="max-width:520px;margin:0 auto;background:#ffffff;border-radius:12px;padding:32px;border:1px solid #e2e8f0;">
                    <h2 style="color:#0f172a;margin-top:0">Welcome, %s!</h2>
                    <p style="color:#334155">Confirm your email address to activate your ExportPlatform account.</p>
                    <p style="color:#64748b;font-size:13px">Your verification code (valid for %d hours):</p>
                    <div style="letter-spacing:8px;font-size:28px;font-weight:bold;color:#0a2540;background:#f1f5f9;padding:14px 20px;border-radius:8px;text-align:center;">%s</div>
                    <p style="color:#64748b;font-size:13px">or <a href="%s" style="color:#c9a227">click here to verify</a>.</p>
                    <p style="color:#94a3b8;font-size:12px">If you did not create this account, you can safely ignore this email.</p>
                  </div>
                </body></html>
                """.formatted(escapeHtml(user.getFullName()), verificationExpirationHours,
                rawToken, escapeHtml(link));
    }

    private String buildLoginOtpEmail(User user, String otp) {
        return """
                <html><body style="font-family:Arial,sans-serif;background:#f8fafc;padding:24px;">
                  <div style="max-width:520px;margin:0 auto;background:#ffffff;border-radius:12px;padding:32px;border:1px solid #e2e8f0;">
                    <h2 style="color:#0f172a;margin-top:0">Hi %s,</h2>
                    <p style="color:#334155">Use this one-time code to finish signing in to your ExportPlatform account.</p>
                    <div style="letter-spacing:8px;font-size:28px;font-weight:bold;color:#0a2540;background:#f1f5f9;padding:14px 20px;border-radius:8px;text-align:center;">%s</div>
                    <p style="color:#64748b;font-size:13px">The code expires in %d minutes. Do not share it with anyone.</p>
                    <p style="color:#94a3b8;font-size:12px">If you did not try to sign in, someone may have your password - please reset it.</p>
                  </div>
                </body></html>
                """.formatted(escapeHtml(user.getFullName()), otp, loginOtpTtlMinutes);
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
