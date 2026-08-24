package com.company.exportplatform.service.storage;

import com.company.exportplatform.exception.BadRequestException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Year;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Cloudinary private raw-resource storage via the signed REST API (no SDK).
 * Active when app.storage.provider=cloudinary; requires CLOUDINARY_* config.
 * Assets are private and always streamed back through the backend so
 * permission checks apply on every download.
 */
@Component
@Slf4j
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "cloudinary")
public class CloudinaryFileStorage implements FileStorage {

    private static final String UPLOAD_URL = "https://api.cloudinary.com/v1_1/%s/raw/upload";
    private static final String DESTROY_URL = "https://api.cloudinary.com/v1_1/%s/raw/destroy";

    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;
    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public CloudinaryFileStorage(@Value("${cloudinary.cloud-name}") String cloudName,
                                 @Value("${cloudinary.api-key}") String apiKey,
                                 @Value("${cloudinary.api-secret}") String apiSecret) {
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        if (isBlank(cloudName) || isBlank(apiKey) || isBlank(apiSecret)) {
            throw new IllegalStateException(
                    "app.storage.provider=cloudinary but CLOUDINARY_* credentials are missing");
        }
    }

    @Override
    public String store(byte[] bytes, String originalFilename, String contentType) {
        String publicId = Year.now().getValue() + "/" + UUID.randomUUID();
        Map<String, String> params = baseParams();
        params.put("public_id", publicId);

        StringBuilder body = new StringBuilder();
        for (Map.Entry<String, String> e : params.entrySet()) {
            body.append(part(e.getKey(), e.getValue()));
        }
        body.append("--").append(BOUNDARY).append("\r\n")
                .append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                .append(sanitizeFilename(originalFilename)).append("\"\r\n")
                .append("Content-Type: application/octet-stream\r\n\r\n");
        String head = body.toString();
        String tail = "\r\n--" + BOUNDARY + "--\r\n";

        byte[] payload = new byte[head.getBytes(StandardCharsets.UTF_8).length
                + bytes.length + tail.getBytes(StandardCharsets.UTF_8).length];
        int i = 0;
        for (byte b : head.getBytes(StandardCharsets.UTF_8)) { payload[i++] = b; }
        System.arraycopy(bytes, 0, payload, i, bytes.length);
        i += bytes.length;
        for (byte b : tail.getBytes(StandardCharsets.UTF_8)) { payload[i++] = b; }

        String url = UPLOAD_URL.formatted(cloudName);
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "multipart/form-data; boundary=" + BOUNDARY)
                .POST(HttpRequest.BodyPublishers.ofByteArray(payload))
                .build();
        JsonNode json = send(request, "upload");
        return json.path("public_id").asText(publicId).replace("/", "__");
    }

    @Override
    public byte[] retrieve(String publicId) {
        String id = publicId.replace("__", "/");
        long expiry = epochSeconds() + 300;
        // Signed private-delivery URL; signature covers sorted "expiry&timestamp" params.
        Map<String, String> signedParams = new LinkedHashMap<>();
        signedParams.put("expiry", String.valueOf(expiry));
        signedParams.put("timestamp", String.valueOf(epochSeconds()));
        String sig = signature(signedParams);
        String url = "https://res.cloudinary.com/" + cloudName + "/raw/private/" + id
                + "?timestamp=" + signedParams.get("timestamp")
                + "&expiry=" + expiry
                + "&signature=" + sig;
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
        try {
            HttpResponse<byte[]> response = http.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) {
                throw new BadRequestException("Stored file could not be fetched from cloud storage");
            }
            return response.body();
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Cloudinary fetch failed", ex);
        }
    }

    @Override
    public void delete(String publicId) {
        Map<String, String> params = baseParams();
        params.put("public_id", publicId.replace("__", "/"));
        String form = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + urlEncodeValue(e.getValue()))
                .collect(Collectors.joining("&"))
                + "&signature=" + signature(params);
        HttpRequest request = HttpRequest.newBuilder(URI.create(DESTROY_URL.formatted(cloudName)))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form, StandardCharsets.UTF_8))
                .build();
        send(request, "destroy");
    }

    @Override
    public boolean servedByBackend() {
        return true;
    }

    // ---------- internals ----------

    private static final String BOUNDARY = "----epupload" + UUID.randomUUID().toString().replace("-", "");

    private Map<String, String> baseParams() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("type", "private");
        params.put("resource_type", "raw");
        params.put("timestamp", String.valueOf(epochSeconds()));
        params.put("api_key", apiKey);
        return params;
    }

    /** Cloudinary auth signature: SHA-1 of sorted k=v params (excluding api_key/file/signature) + secret. */
    private String signature(Map<String, String> params) {
        String flat = params.entrySet().stream()
                .filter(e -> !"api_key".equals(e.getKey()))
                .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
        return sha1Hex(flat + apiSecret);
    }

    private JsonNode send(HttpRequest request, String action) {
        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode json = mapper.readTree(response.body());
            if (response.statusCode() >= 400) {
                log.error("Cloudinary {} failed: {}", action, response.body());
                throw new IllegalStateException("Cloudinary " + action + " failed");
            }
            return json;
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Cloudinary " + action + " request error", ex);
        }
    }

    private static String part(String name, String value) {
        return "--" + BOUNDARY + "\r\n"
                + "Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n"
                + value + "\r\n";
    }

    private static String sanitizeFilename(String filename) {
        return filename == null || filename.isBlank() ? "document" : filename.replaceAll("[\"\\\r\n]", "");
    }

    private static String urlEncodeValue(String value) {
        return value == null ? "" : java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static long epochSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String sha1Hex(String payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot compute SHA-1 signature", ex);
        }
    }
}
