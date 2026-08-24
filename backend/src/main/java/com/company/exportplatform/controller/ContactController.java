package com.company.exportplatform.controller;

import com.company.exportplatform.dto.request.ContactMessageRequest;
import com.company.exportplatform.dto.response.ApiResponse;
import com.company.exportplatform.entity.ContactMessage;
import com.company.exportplatform.repository.ContactMessageRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactMessageRepository contactMessageRepository;
    private final com.company.exportplatform.security.RateLimitService rateLimitService;

    @PostMapping
    @ResponseBody
    public ApiResponse<Void> submit(@Valid @RequestBody ContactMessageRequest request,
                                    jakarta.servlet.http.HttpServletRequest httpRequest) {
        String forwarded = httpRequest.getHeader("X-Forwarded-For");
        String ip = forwarded != null && !forwarded.isBlank()
                ? forwarded.split(",")[0].trim() : httpRequest.getRemoteAddr();
        rateLimitService.checkContact(ip);
        ContactMessage message = new ContactMessage();
        message.setFullName(request.getFullName().trim());
        message.setEmail(request.getEmail().trim().toLowerCase());
        message.setPhone(blankToNull(request.getPhone()));
        message.setCompany(blankToNull(request.getCompany()));
        message.setSubject(blankToNull(request.getSubject()));
        message.setMessage(request.getMessage().trim());
        contactMessageRepository.save(message);
        return ApiResponse.ok("Thank you for reaching out. Our team will get back to you shortly.");
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
