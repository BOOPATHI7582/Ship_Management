package com.company.exportplatform.service;

import com.company.exportplatform.dto.request.AdminUserUpdateRequest;
import com.company.exportplatform.dto.response.AdminUserResponse;
// import com.company.exportplatform.entity.Client;
import com.company.exportplatform.entity.Role;
import com.company.exportplatform.entity.User;
import com.company.exportplatform.entity.enums.RoleName;
import com.company.exportplatform.exception.ResourceNotFoundException;
import com.company.exportplatform.repository.ClientRepository;
import com.company.exportplatform.repository.RoleRepository;
import com.company.exportplatform.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ClientRepository clientRepository;

    @Transactional(readOnly = true)
    public Page<AdminUserResponse> listUsers(String search, String role, Pageable pageable) {
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (search != null && !search.isBlank()) {
                String term = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("email")), term),
                        cb.like(cb.lower(root.get("fullName")), term)));
            }
            if (role != null && !role.isBlank()) {
                predicates.add(cb.equal(root.get("role").get("name"), RoleName.valueOf(role)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return userRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<com.company.exportplatform.dto.response.AdminClientResponse> listClients(Pageable pageable) {
        return userRepository.findByRoleName(RoleName.CLIENT, pageable).map(user -> {
            com.company.exportplatform.entity.Client profile =
                    clientRepository.findByUserId(user.getId()).orElse(null);
            return new com.company.exportplatform.dto.response.AdminClientResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getCompanyName(),
                    user.getPhone(),
                    user.getCountry(),
                    user.isActive(),
                    profile != null ? profile.getGstin() : null,
                    profile != null ? profile.getCity() : null,
                    profile != null ? profile.getState() : null,
                    user.getLastLoginAt(),
                    user.getCreatedAt()
            );
        });
    }

    @Transactional
    public AdminUserResponse updateUser(Long id, AdminUserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName().trim());
        }
        if (request.getCompanyName() != null) {
            user.setCompanyName(request.getCompanyName().isBlank() ? null : request.getCompanyName().trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().isBlank() ? null : request.getPhone().trim());
        }
        if (request.getCountry() != null) {
            user.setCountry(request.getCountry().isBlank() ? null : request.getCountry().trim());
        }
        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }
        if (request.getRole() != null && !request.getRole().isBlank()) {
            Role newRole = roleRepository.findByName(RoleName.valueOf(request.getRole()))
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
            user.setRole(newRole);
        }
        return toResponse(userRepository.save(user));
    }

    private AdminUserResponse toResponse(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getCompanyName(),
                user.getPhone(),
                user.getCountry(),
                user.getRole() != null ? user.getRole().getName().name() : null,
                user.isActive(),
                user.getLastLoginAt(),
                user.getCreatedAt()
        );
    }
}
