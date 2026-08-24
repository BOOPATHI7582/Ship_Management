package com.company.exportplatform.controller;

import com.company.exportplatform.dto.response.ApiResponse;
import com.company.exportplatform.dto.response.DocumentResponse;
import com.company.exportplatform.entity.enums.DocumentOwnerType;
import com.company.exportplatform.exception.BadRequestException;
import com.company.exportplatform.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@RestController
@RequestMapping("/api/manager/documents")
@RequiredArgsConstructor
public class ManagerDocumentController {

    private final DocumentService documentService;

    @GetMapping
    public ApiResponse<?> list(
            @RequestParam String ownerType,
            @RequestParam Long ownerId) {
        return ApiResponse.ok(null, documentService.listForStaff(
                parseOwnerType(ownerType), ownerId));
    }

    @PostMapping("/upload")
    public ApiResponse<DocumentResponse> upload(
            Authentication authentication,
            @RequestParam("file") MultipartFile file,
            @RequestParam String ownerType,
            @RequestParam Long ownerId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String title) {
        return ApiResponse.ok("Document attached", documentService.uploadForStaff(
                authentication.getName(), file, parseOwnerType(ownerType), ownerId, category, title));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        return toFile(documentService.downloadForStaff(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, Authentication authentication) {
        documentService.deleteForStaff(id, authentication.getName());
        return ApiResponse.ok("Document deleted", null);
    }

    static DocumentOwnerType parseOwnerType(String raw) {
        try {
            return DocumentOwnerType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Unknown owner type: " + raw
                    + " (allowed: " + Arrays.toString(DocumentOwnerType.values()) + ")");
        }
    }

    static ResponseEntity<byte[]> toFile(DocumentService.StoredDownload download) {
        String encoded = URLEncoder.encode(download.filename(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encoded)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(download.bytes());
    }
}
