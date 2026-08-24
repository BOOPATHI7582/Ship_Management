package com.company.exportplatform.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserUpdateRequest {

    @Size(max = 150)
    private String fullName;

    @Size(max = 200)
    private String companyName;

    @Size(max = 30)
    private String phone;

    @Size(max = 80)
    private String country;

    private Boolean active;

    @Pattern(regexp = "ADMIN|CLIENT|SHIP_MANAGER", message = "Invalid role")
    private String role;
}
