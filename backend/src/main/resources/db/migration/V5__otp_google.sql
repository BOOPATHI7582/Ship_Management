-- V5: login OTP + Google sign-in support
--   * users.google_sub      - Google account subject for OAuth sign-in
--   * users.password_hash   - now nullable (Google-only accounts have no password)
--   * email_verifications.attempts - cap on wrong OTP tries
--   * otp_codes             - short-lived login OTP codes (hashed, single-use)

ALTER TABLE users
    MODIFY COLUMN password_hash VARCHAR(100) NULL,
    ADD COLUMN google_sub VARCHAR(255) NULL AFTER password_hash,
    ADD KEY idx_users_google_sub (google_sub);

ALTER TABLE email_verifications
    ADD COLUMN attempts INT NOT NULL DEFAULT 0 AFTER verified_at;

CREATE TABLE otp_codes (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    user_id     BIGINT        NOT NULL,
    purpose     VARCHAR(30)   NOT NULL,
    code_hash   VARCHAR(128)  NOT NULL,
    attempts    INT           NOT NULL DEFAULT 0,
    expires_at  DATETIME(6)   NOT NULL,
    used_at     DATETIME(6)   NULL,
    created_at  DATETIME(6)   NULL,
    updated_at  DATETIME(6)   NULL,
    PRIMARY KEY (id),
    KEY idx_otp_codes_user_purpose (user_id, purpose),
    CONSTRAINT fk_otp_codes_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;