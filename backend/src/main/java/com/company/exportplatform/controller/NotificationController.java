package com.company.exportplatform.controller;

import com.company.exportplatform.dto.response.ApiResponse;
import com.company.exportplatform.dto.response.NotificationResponse;
import com.company.exportplatform.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<Page<NotificationResponse>> list(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<NotificationResponse> result = notificationService.listForCurrentUser(authentication.getName(),
                PageRequest.of(page, Math.min(size, 50), Sort.by(Sort.Direction.DESC, "createdAt")));
        return ApiResponse.ok("Notifications", result);
    }

    @PutMapping("/{id}/read")
    public ApiResponse<Void> markRead(Authentication authentication, @PathVariable Long id) {
        notificationService.markRead(authentication.getName(), id);
        return ApiResponse.ok("Notification marked as read");
    }
}
