package com.company.exportplatform.service;

import com.company.exportplatform.exception.BadRequestException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Verifies Google sign-in ID tokens against Google's tokeninfo endpoint so
 * the app can issue its own JWT without an OAuth redirect dance. The expected
 * audience (GOOGLE_CLIENT_ID) is enforced only when configured, so the flow
 * can be wired before the Client ID exists.
 */
@Service
@Slf4j
public class GoogleLoginService {

    private static final String TOKENINFO_URL =
            "https://oauth2.googleapis.com/tokeninfo?id_token=";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String expectedAudience;

    public GoogleLoginService(ObjectMapper objectMapper,
                              @Value("${app.google.client-id:}") String clientId) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5)).build();
        this.expectedAudience = clientId == null ? "" : clientId.trim();
    }

    public record GoogleProfile(String sub, String email, String name, boolean emailVerified) {
    }

    /**
     * @throws BadRequestException when the token is missing, expired, or fails
     *                             Google's remote verification.
     */
    public GoogleProfile verify(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new BadRequestException("Missing Google ID token");
        }
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TOKENINFO_URL + idToken))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.warn("Google tokeninfo rejected token (status {})", response.statusCode());
                throw new BadRequestException("Google sign-in could not be verified. Please try again.");
            }
            JsonNode json = objectMapper.readTree(response.body());
            String aud = text(json, "aud");
            if (!expectedAudience.isBlank() && !expectedAudience.equals(aud)) {
                log.warn("Google token audience mismatch");
                throw new BadRequestException("Google sign-in could not be verified. Please try again.");
            }
            long exp = json.has("exp") ? json.get("exp").asLong() : 0;
            if (exp > 0 && exp * 1000L < System.currentTimeMillis()) {
                throw new BadRequestException("Google sign-in expired. Please try again.");
            }
            String email = text(json, "email");
            String sub = text(json, "sub");
            if (email == null || sub == null) {
                throw new BadRequestException("Google sign-in did not provide an email address.");
            }
            boolean emailVerified = json.has("email_verified") && json.get("email_verified").asBoolean();
            if (!emailVerified) {
                throw new BadRequestException("Your Google account email is not verified.");
            }
            return new GoogleProfile(sub, email.toLowerCase(), text(json, "name"), true);
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Google token verification failed: {}", ex.getMessage());
            throw new BadRequestException("Google sign-in is temporarily unavailable. Please try again.");
        }
    }

    private String text(JsonNode json, String field) {
        JsonNode node = json.get(field);
        return node == null || node.isNull() ? null : node.asText();
    }
}