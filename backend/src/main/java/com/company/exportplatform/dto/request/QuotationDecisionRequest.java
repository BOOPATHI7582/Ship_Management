package com.company.exportplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record QuotationDecisionRequest(
        @NotBlank @Pattern(regexp = "ACCEPT|REJECT", flags = Pattern.Flag.CASE_INSENSITIVE) String decision,
        @Size(max = 500) String reason
) {
}
