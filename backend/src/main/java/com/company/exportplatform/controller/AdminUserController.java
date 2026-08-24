package com.company.exportplatform.controller;

import com.company.exportplatform.dto.request.AdminUserUpdateRequest;
import com.company.exportplatform.dto.response.AdminUserResponse;
import com.company.exportplatform.dto.response.ApiResponse;
import com.company.exportplatform.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping("/users")
    public ApiResponse<Page<AdminUserResponse>> users(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<AdminUserResponse> result = adminUserService.listUsers(search, role,
                PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt")));
        return ApiResponse.ok("Users", result);
    }

    @GetMapping("/clients")
    public ApiResponse<Page<com.company.exportplatform.dto.response.AdminClientResponse>> clients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<com.company.exportplatform.dto.response.AdminClientResponse> result =
                adminUserService.listClients(
                        PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt")));
        return ApiResponse.ok("Clients", result);
    }

    @PutMapping("/users/{id}")
    public ApiResponse<AdminUserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody AdminUserUpdateRequest request) {
        return ApiResponse.ok("User updated", adminUserService.updateUser(id, request));
    }
}
