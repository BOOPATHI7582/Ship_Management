package com.company.exportplatform.controller;

import com.company.exportplatform.dto.response.ApiResponse;
import com.company.exportplatform.dto.response.ContactMessageResponse;
import com.company.exportplatform.service.ContactInboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Staff inbox over public contact-form submissions.
 * Accessible to ADMIN and SHIP_MANAGER via /api/manager/** rules.
 */
@RestController
@RequestMapping("/api/manager/contact")
@RequiredArgsConstructor
public class ManagerContactController {

    private final ContactInboxService contactInboxService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SHIP_MANAGER')")
    public ApiResponse<Page<ContactMessageResponse>> list(
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok("Contact messages",
                contactInboxService.list(status, search, page, size));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyRole('ADMIN','SHIP_MANAGER')")
    public ApiResponse<Long> unreadCount() {
        return ApiResponse.ok("Unread count", contactInboxService.unreadCount());
    }

    @PostMapping("/{id}/handle")
    @PreAuthorize("hasAnyRole('ADMIN','SHIP_MANAGER')")
    public ApiResponse<ContactMessageResponse> handle(@PathVariable Long id,
            Authentication authentication) {
        return ApiResponse.ok("Marked as handled",
                contactInboxService.markHandled(id, authentication.getName()));
    }

    @PostMapping("/{id}/reopen")
    @PreAuthorize("hasAnyRole('ADMIN','SHIP_MANAGER')")
    public ApiResponse<ContactMessageResponse> reopen(@PathVariable Long id,
            Authentication authentication) {
        return ApiResponse.ok("Reopened",
                contactInboxService.reopen(id, authentication.getName()));
    }
}
