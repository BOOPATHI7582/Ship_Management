package com.company.exportplatform.service.storage;

import com.company.exportplatform.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Local-disk storage under app.storage.local-dir (default ./uploads).
 * Files are addressed by "<yyyy>/<uuid>.<ext>" and streamed back through the
 * authenticated download endpoint.
 */
@Component
@Slf4j
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalFileStorage implements FileStorage {

    private final Path baseDir;

    public LocalFileStorage(@Value("${app.storage.local-dir:uploads}") String baseDir) {
        this.baseDir = Path.of(baseDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.baseDir);
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot create storage directory " + this.baseDir, ex);
        }
    }

    @Override
    public String store(byte[] bytes, String originalFilename, String contentType) {
        if (bytes == null || bytes.length == 0) {
            throw new BadRequestException("File is empty");
        }
        String ext = extension(originalFilename);
        String year = String.valueOf(java.time.Year.now().getValue());
        String key = year + "/" + UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
        Path target = resolve(key);
        try {
            Files.createDirectories(target.getParent());
            try (InputStream in = new java.io.ByteArrayInputStream(bytes)) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return key;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to persist file", ex);
        }
    }

    @Override
    public byte[] retrieve(String publicId) {
        Path path = resolve(publicId);
        if (!Files.exists(path)) {
            throw new BadRequestException("Stored file is missing");
        }
        try {
            return Files.readAllBytes(path);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read stored file", ex);
        }
    }

    @Override
    public void delete(String publicId) {
        try {
            Files.deleteIfExists(resolve(publicId));
        } catch (IOException ex) {
            log.warn("Could not delete stored file {}", publicId, ex);
        }
    }

    @Override
    public boolean servedByBackend() {
        return true;
    }

    private Path resolve(String publicId) {
        if (publicId == null || publicId.isBlank() || publicId.contains("..")) {
            throw new BadRequestException("Invalid file reference");
        }
        return baseDir.resolve(publicId).normalize();
    }

    private static String extension(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        return dot >= 0 && dot < filename.length() - 1 ? filename.substring(dot + 1).toLowerCase() : "";
    }
}
