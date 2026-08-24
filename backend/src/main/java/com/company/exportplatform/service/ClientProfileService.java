package com.company.exportplatform.service;

import com.company.exportplatform.dto.response.ClientProfileResponse;
import com.company.exportplatform.dto.request.ClientProfileRequest;
import com.company.exportplatform.entity.Client;
import com.company.exportplatform.entity.User;
import com.company.exportplatform.exception.ResourceNotFoundException;
import com.company.exportplatform.repository.ClientRepository;
import com.company.exportplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClientProfileService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;

    @Transactional(readOnly = true)
    public ClientProfileResponse get(String email) {
        User user = requireUser(email);
        Client client = clientRepository.findByUserId(user.getId()).orElse(null);
        return toResponse(user, client);
    }

    @Transactional
    public ClientProfileResponse update(String email, ClientProfileRequest request) {
        User user = requireUser(email);
        user.setFullName(request.getFullName().trim());
        user.setCompanyName(trimOrNull(request.getCompanyName()));
        user.setPhone(trimOrNull(request.getPhone()));
        user.setCountry(trimOrNull(request.getCountry()));

        Client client = clientRepository.findByUserId(user.getId()).orElseGet(() -> {
            Client fresh = new Client();
            fresh.setUser(user);
            return fresh;
        });
        client.setGstin(blankToNull(request.getGstin()));
        client.setAddressLine1(blankToNull(request.getAddressLine1()));
        client.setAddressLine2(blankToNull(request.getAddressLine2()));
        client.setCity(blankToNull(request.getCity()));
        client.setState(blankToNull(request.getState()));
        client.setPostalCode(blankToNull(request.getPostalCode()));
        client.setCountry(user.getCountry());
        clientRepository.save(client);

        return toResponse(user, client);
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private String trimOrNull(String value) {
        return blankToNull(value);
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private ClientProfileResponse toResponse(User user, Client client) {
        return new ClientProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getCompanyName(),
                user.getPhone(),
                user.getCountry(),
                client != null ? client.getGstin() : null,
                client != null ? client.getAddressLine1() : null,
                client != null ? client.getAddressLine2() : null,
                client != null ? client.getCity() : null,
                client != null ? client.getState() : null,
                client != null ? client.getPostalCode() : null,
                user.getLastLoginAt()
        );
    }
}
