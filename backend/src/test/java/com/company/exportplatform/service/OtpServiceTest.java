package com.company.exportplatform.service;

import com.company.exportplatform.entity.OtpCode;
import com.company.exportplatform.entity.User;
import com.company.exportplatform.exception.BadRequestException;
import com.company.exportplatform.repository.OtpCodeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private OtpCodeRepository repository;

    private OtpService service;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        service = new OtpService(repository);
    }

    private static User user() {
        User user = new User();
        user.setId(1L);
        return user;
    }

    private static OtpCode otp(String code, LocalDateTime expiresAt, int attempts) {
        OtpCode otp = new OtpCode();
        otp.setUser(user());
        otp.setPurpose(OtpService.PURPOSE_LOGIN);
        otp.setCodeHash(sha256(code));
        otp.setExpiresAt(expiresAt);
        otp.setAttempts(attempts);
        return otp;
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Test
    void issueInvalidatesOldCodesAndStoresHashOnly() {
        String code = service.issue(user(), OtpService.PURPOSE_LOGIN, 10);

        assertThat(code).matches("\\d{6}");
        ArgumentCaptor<OtpCode> captor = ArgumentCaptor.forClass(OtpCode.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getCodeHash()).isEqualTo(sha256(code));
        assertThat(captor.getValue().getCodeHash()).isNotEqualTo(code);
        verify(repository).deleteByUserIdAndPurposeAndUsedAtIsNull(1L, OtpService.PURPOSE_LOGIN);
    }

    @Test
    void verifyAcceptsMatchingCodeAndMarksUsed() {
        OtpCode code = otp("123456", LocalDateTime.now().plusMinutes(5), 0);
        when(repository.findFirstByUserIdAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc(1L, OtpService.PURPOSE_LOGIN))
                .thenReturn(Optional.of(code));

        service.verify(user(), OtpService.PURPOSE_LOGIN, "123456");

        assertThat(code.getUsedAt()).isNotNull();
        verify(repository).save(code);
    }

    @Test
    void verifyRejectsWrongCodeAndIncrementsAttempts() {
        OtpCode code = otp("999999", LocalDateTime.now().plusMinutes(5), 0);
        when(repository.findFirstByUserIdAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc(1L, OtpService.PURPOSE_LOGIN))
                .thenReturn(Optional.of(code));

        assertThatThrownBy(() -> service.verify(user(), OtpService.PURPOSE_LOGIN, "000000"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Incorrect");

        assertThat(code.getAttempts()).isEqualTo(1);
    }

    @Test
    void verifyRejectsExpiredCode() {
        OtpCode code = otp("123456", LocalDateTime.now().minusMinutes(1), 0);
        when(repository.findFirstByUserIdAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc(1L, OtpService.PURPOSE_LOGIN))
                .thenReturn(Optional.of(code));

        assertThatThrownBy(() -> service.verify(user(), OtpService.PURPOSE_LOGIN, "123456"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("expired");
        verify(repository).delete(code);
    }

    @Test
    void verifyLocksAfterTooManyAttempts() {
OtpCode code = otp("123456", LocalDateTime.now().plusMinutes(5), 5);
        when(repository.findFirstByUserIdAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc(1L, OtpService.PURPOSE_LOGIN))
                .thenReturn(Optional.of(code));

        assertThatThrownBy(() -> service.verify(user(), OtpService.PURPOSE_LOGIN, "123456"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Too many failed attempts");
        verify(repository).delete(code);
    }
}