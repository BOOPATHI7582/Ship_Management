package com.company.exportplatform.service;

import com.company.exportplatform.dto.request.LoginRequest;
import com.company.exportplatform.dto.response.AuthResponse;
import com.company.exportplatform.entity.Role;
import com.company.exportplatform.entity.User;
import com.company.exportplatform.entity.enums.RoleName;
import com.company.exportplatform.exception.BadRequestException;
import com.company.exportplatform.repository.ClientRepository;
import com.company.exportplatform.repository.PasswordResetRepository;
import com.company.exportplatform.repository.RoleRepository;
import com.company.exportplatform.repository.UserRepository;
import com.company.exportplatform.security.JwtService;
import com.company.exportplatform.security.LoginLockoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceLoginTest {

    private static final String EMAIL = "lock@test.com";

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private PasswordResetRepository passwordResetRepository;
    @Mock
    private com.company.exportplatform.repository.EmailVerificationRepository emailVerificationRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private MailService mailService;
    @Mock
    private LoginLockoutService loginLockoutService;
    @Mock
    private OtpService otpService;
    @Mock
    private GoogleLoginService googleLoginService;

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(userRepository, roleRepository, clientRepository,
                passwordResetRepository, emailVerificationRepository, passwordEncoder,
                authenticationManager, jwtService, mailService, loginLockoutService,
                new AuditService(org.mockito.Mockito.mock(com.company.exportplatform.repository.AuditLogRepository.class),
                        new com.fasterxml.jackson.databind.ObjectMapper()),
                otpService, googleLoginService);
    }

    private LoginRequest login(String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(EMAIL);
        request.setPassword(password);
        return request;
    }

    @Test
    @DisplayName("failed credentials record a lockout failure and audit entry")
    void failedLoginRecordsFailure() {
        doThrow(new BadCredentialsException("bad")).when(authenticationManager)
                .authenticate(any());

        assertThatThrownBy(() -> service.login(login("wrong")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid email or password");

        verify(loginLockoutService).recordFailure(EMAIL);
        verify(loginLockoutService, never()).recordSuccess(anyString());
    }

    @Test
    @DisplayName("successful login resets lockout and issues a session token directly (no OTP step)")
    void successfulLoginIssuesToken() {
        User user = new User();
        user.setId(9L);
        user.setEmail(EMAIL);
        user.setActive(true);
        user.setPasswordHash("hash");
        Role role = new Role();
        role.setName(RoleName.ADMIN);
        user.setRole(role);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(jwtService.getExpirationMs()).thenReturn(86400000L);

        AuthResponse response = service.login(login("right"));

        verify(loginLockoutService).checkAllowed(EMAIL);
        verify(loginLockoutService).recordSuccess(EMAIL);
        verify(otpService, never()).issue(any(), any(), anyInt());
        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
        assertThat(response.isRequiresOtp()).isFalse();
        assertThat(response.getUser().getEmail()).isEqualTo(EMAIL);
        assertThat(response.getUser().getRole()).isEqualTo(RoleName.ADMIN);
    }

    @Test
    @DisplayName("verifying the login OTP issues the session token")
    void verifyLoginOtpIssuesToken() {
        User user = new User();
        user.setId(9L);
        user.setEmail(EMAIL);
        user.setActive(true);
        Role role = new Role();
        role.setName(RoleName.ADMIN);
        user.setRole(role);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(jwtService.getExpirationMs()).thenReturn(1000L);

        com.company.exportplatform.dto.request.LoginOtpRequest request =
                new com.company.exportplatform.dto.request.LoginOtpRequest(EMAIL, "123456");

        AuthResponse response = service.verifyLoginOtp(request);

        verify(otpService).verify(user, OtpService.PURPOSE_LOGIN, "123456");
        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
    }

    @Test
    @DisplayName("deactivated accounts are refused even with valid credentials")
    void deactivatedAccountRefused() {
        User user = new User();
        user.setId(10L);
        user.setEmail(EMAIL);
        user.setActive(false);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.login(login("right")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("deactivated");

        verify(loginLockoutService, never()).recordSuccess(anyString());
    }
}
