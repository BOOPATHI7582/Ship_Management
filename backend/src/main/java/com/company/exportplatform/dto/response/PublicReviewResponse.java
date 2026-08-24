package com.company.exportplatform.dto.response;

public record PublicReviewResponse(
        Long id,
        int rating,
        String title,
        String reviewText,
        String clientName,
        String companyName
) {
}
