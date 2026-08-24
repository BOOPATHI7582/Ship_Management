package com.company.exportplatform.controller;

import com.company.exportplatform.dto.request.ClientProfileRequest;
import com.company.exportplatform.dto.response.ApiResponse;
import com.company.exportplatform.dto.response.ClientProfileResponse;
import com.company.exportplatform.dto.response.DashboardSummaryResponse;
import com.company.exportplatform.service.ClientProfileService;
import com.company.exportplatform.service.DashboardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
public class ClientController {

    private final DashboardService dashboardService;
    private final ClientProfileService clientProfileService;

    @GetMapping("/dashboard")
    public ApiResponse<DashboardSummaryResponse> dashboard(Authentication authentication) {
        return ApiResponse.ok("Dashboard summary",
                dashboardService.summary(authentication.getName()));
    }

    @GetMapping("/profile")
    public ApiResponse<ClientProfileResponse> profile(Authentication authentication) {
        return ApiResponse.ok("Profile", clientProfileService.get(authentication.getName()));
    }

    @PutMapping("/profile")
    public ApiResponse<ClientProfileResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody ClientProfileRequest request) {
        return ApiResponse.ok("Profile updated",
                clientProfileService.update(authentication.getName(), request));
    }
}
