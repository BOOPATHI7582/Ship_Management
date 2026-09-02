package com.company.exportplatform.controller;

import com.company.exportplatform.dto.request.ForgotPasswordRequest;
import com.company.exportplatform.dto.request.LoginRequest;
import com.company.exportplatform.dto.request.RegisterRequest;
import com.company.exportplatform.dto.request.ResetPasswordRequest;
import com.company.exportplatform.dto.response.ApiResponse;
import com.company.exportplatform.dto.response.AuthResponse;
import com.company.exportplatform.dto.response.UserResponse;
import com.company.exportplatform.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final com.company.exportplatform.security.RateLimitService rateLimitService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<com.company.exportplatform.dto.response.RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        rateLimitService.checkRegister(clientIp(httpRequest));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Account created. Please verify your email to activate it.",
                        authService.register(request)));
    }

    @PostMapping("/verify-email")
    public ApiResponse<AuthResponse> verifyEmail(
            @Valid @RequestBody com.company.exportplatform.dto.request.VerifyEmailRequest request) {
        return ApiResponse.ok("Email verified successfully. You can now log in.",
                authService.verifyEmail(request.token(), request.email()));
    }

    @PostMapping("/resend-verification")
    public ApiResponse<Void> resendVerification(
            @Valid @RequestBody com.company.exportplatform.dto.request.ResendVerificationRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        rateLimitService.checkForgotPassword(clientIp(httpRequest));
        return authService.resendVerification(request.email());
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok("If your sign-in is secured, a verification code has been sent.",
                authService.login(request));
    }

    @PostMapping("/login/otp")
    public ApiResponse<AuthResponse> verifyLoginOtp(
            @Valid @RequestBody com.company.exportplatform.dto.request.LoginOtpRequest request) {
        return ApiResponse.ok("Logged in successfully", authService.verifyLoginOtp(request));
    }

    @PostMapping("/login/otp/resend")
    public ApiResponse<Void> resendLoginOtp(
            @Valid @RequestBody com.company.exportplatform.dto.request.ResendVerificationRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        rateLimitService.checkForgotPassword(clientIp(httpRequest));
        return authService.resendLoginOtp(request.email());
    }

    @PostMapping("/google")
    public ApiResponse<AuthResponse> googleLogin(
            @Valid @RequestBody com.company.exportplatform.dto.request.GoogleLoginRequest request) {
        return ApiResponse.ok("Logged in successfully", authService.googleLogin(request));
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> me(Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        return ApiResponse.ok("Current user", authService.currentUser(email));
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        rateLimitService.checkForgotPassword(clientIp(httpRequest));
        return authService.forgotPassword(request);
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(request);
    }

    private String clientIp(jakarta.servlet.http.HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
