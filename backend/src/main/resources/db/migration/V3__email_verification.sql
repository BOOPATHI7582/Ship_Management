-- V3: email verification tokens for new registrations
CREATE TABLE email_verifications (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    user_id     BIGINT        NOT NULL,
    token_hash  VARCHAR(128)  NOT NULL,
    expires_at  DATETIME(6)   NOT NULL,
    verified_at DATETIME(6)   NULL,
    created_at  DATETIME(6)   NULL,
    updated_at  DATETIME(6)   NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_email_verifications_token (token_hash),
    KEY idx_email_verifications_user (user_id),
    CONSTRAINT fk_email_verifications_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
