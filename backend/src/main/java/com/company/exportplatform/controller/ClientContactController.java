package com.company.exportplatform.controller;

import com.company.exportplatform.dto.response.ApiResponse;
import com.company.exportplatform.dto.response.ContactMessageResponse;
import com.company.exportplatform.service.ContactInboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Clients see the contact-form messages they submitted themselves,
 * matched by their account email, with handling status.
 */
@RestController
@RequestMapping("/api/client/contact-messages")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
public class ClientContactController {

    private final ContactInboxService contactInboxService;

    @GetMapping
    public ApiResponse<Page<ContactMessageResponse>> myMessages(Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok("My messages", contactInboxService.myMessages(authentication.getName(), page, size));
    }
}
