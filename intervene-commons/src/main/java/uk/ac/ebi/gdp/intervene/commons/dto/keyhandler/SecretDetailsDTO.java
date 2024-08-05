package uk.ac.ebi.gdp.intervene.commons.dto.keyhandler;

import java.time.LocalDateTime;

public class SecretDetailsDTO {
    private final String secretId;
    private final String secretIdVersion;
    private final LocalDateTime expiresAt;

    public SecretDetailsDTO(final String secretId,
                            final String secretIdVersion,
                            final LocalDateTime expiresAt) {
        this.secretId = secretId;
        this.secretIdVersion = secretIdVersion;
        this.expiresAt = expiresAt;
    }

    public String getSecretId() {
        return secretId;
    }

    public String getSecretIdVersion() {
        return secretIdVersion;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}
