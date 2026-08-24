package com.company.exportplatform.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactMessageRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 150, message = "Full name must be at most 150 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @Size(max = 30, message = "Phone must be at most 30 characters")
    private String phone;

    @Size(max = 200, message = "Company must be at most 200 characters")
    private String company;

    @Size(max = 255, message = "Subject must be at most 255 characters")
    private String subject;

    @NotBlank(message = "Message is required")
    @Size(max = 5000, message = "Message must be at most 5000 characters")
    private String message;
}
