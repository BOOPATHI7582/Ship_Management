package com.company.exportplatform.controller;

import com.company.exportplatform.dto.response.ApiResponse;
import com.company.exportplatform.dto.response.DocumentResponse;
import com.company.exportplatform.exception.BadRequestException;
import com.company.exportplatform.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/client/documents")
@RequiredArgsConstructor
public class ClientDocumentController {

    private final DocumentService documentService;

    /** ownerType/ownerId optional - omitted returns everything the client owns. */
    @GetMapping
    public ApiResponse<?> list(
            Authentication authentication,
            @RequestParam(required = false) String ownerType,
            @RequestParam(required = false) Long ownerId) {
        if (ownerType != null && ownerId == null || ownerType == null && ownerId != null) {
            throw new BadRequestException("ownerType and ownerId must be provided together");
        }
        return ApiResponse.ok(null, documentService.listMine(authentication.getName(),
                ownerType != null ? ManagerDocumentController.parseOwnerType(ownerType) : null, ownerId));
    }

    @PostMapping("/upload")
    public ApiResponse<DocumentResponse> upload(
            Authentication authentication,
            @RequestParam("file") MultipartFile file,
            @RequestParam String ownerType,
            @RequestParam Long ownerId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String title) {
        return ApiResponse.ok("Document attached", documentService.uploadMine(
                authentication.getName(), file,
                ManagerDocumentController.parseOwnerType(ownerType), ownerId, category, title));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(Authentication authentication, @PathVariable Long id) {
        return ManagerDocumentController.toFile(
                documentService.downloadMine(authentication.getName(), id));
    }
}
