package com.company.exportplatform.service.storage;

/**
 * Binary storage port. Implementations: local disk (dev / self-hosted) and
 * Cloudinary (production, signed uploads) selected via app.storage.provider.
 */
public interface FileStorage {

    /**
     * Persists the bytes and returns an opaque public id that can be resolved
     * back by {@link #retrieve}.
     */
    String store(byte[] bytes, String originalFilename, String contentType);

    byte[] retrieve(String publicId);

    void delete(String publicId);

    /** Whether this provider serves files through the backend download endpoint. */
    boolean servedByBackend();
}
