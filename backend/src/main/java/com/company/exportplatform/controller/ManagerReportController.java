package com.company.exportplatform.controller;

import com.company.exportplatform.dto.response.ApiResponse;
import com.company.exportplatform.dto.response.ReportOverviewResponse;
import com.company.exportplatform.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/manager/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ManagerReportController {

    private final ReportService reportService;

    @GetMapping("/overview")
    public ApiResponse<ReportOverviewResponse> overview() {
        return ApiResponse.ok("Reports overview", reportService.overview());
    }
}
