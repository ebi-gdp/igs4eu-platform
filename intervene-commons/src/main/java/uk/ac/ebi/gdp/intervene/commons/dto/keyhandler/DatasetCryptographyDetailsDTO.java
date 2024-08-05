package uk.ac.ebi.gdp.intervene.commons.dto.keyhandler;

import java.time.LocalDateTime;

public class DatasetCryptographyDetailsDTO extends SecretDetailsDTO {
    private final String publicKey;

    public DatasetCryptographyDetailsDTO(final String secretId,
                                         final String secretIdVersion,
                                         final String publicKey,
                                         final LocalDateTime expiresAt) {
        super(secretId, secretIdVersion, expiresAt);
        this.publicKey = publicKey;
    }

    public String getPublicKey() {
        return publicKey;
    }
}
