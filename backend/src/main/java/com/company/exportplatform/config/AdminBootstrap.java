package com.company.exportplatform.config;

import com.company.exportplatform.entity.Role;
import com.company.exportplatform.entity.User;
import com.company.exportplatform.entity.enums.RoleName;
import com.company.exportplatform.repository.RoleRepository;
import com.company.exportplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        String email = envOrDefault("ADMIN_EMAIL", "admin@exportplatform.com");
        String password = envOrDefault("ADMIN_PASSWORD", "Admin@123");

        if (userRepository.existsByEmail(email)) {
            return;
        }

        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseThrow(() -> new IllegalStateException("ADMIN role missing from roles table"));

        User admin = new User();
        admin.setEmail(email);
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setFullName("Platform Administrator");
        admin.setRole(adminRole);
        admin.setActive(true);
        userRepository.save(admin);
        log.info("Seeded initial admin user: {}", email);
    }

    private String envOrDefault(String key, String fallback) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
