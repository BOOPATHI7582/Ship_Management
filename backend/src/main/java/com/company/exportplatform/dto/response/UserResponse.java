package com.company.exportplatform.dto.response;

import com.company.exportplatform.entity.User;
import com.company.exportplatform.entity.enums.RoleName;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private String fullName;
    private String companyName;
    private String phone;
    private String country;
    private RoleName role;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .companyName(user.getCompanyName())
                .phone(user.getPhone())
                .country(user.getCountry())
                .role(user.getRole().getName())
                .build();
    }
}
