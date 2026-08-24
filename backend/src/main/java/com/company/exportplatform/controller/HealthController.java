package com.company.exportplatform.controller;

import com.company.exportplatform.dto.response.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Value("${spring.application.name}")
    private String applicationName;

    @GetMapping
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.ok("API is running", Map.of(
                "service", applicationName,
                "status", "UP"
        ));
    }
}
