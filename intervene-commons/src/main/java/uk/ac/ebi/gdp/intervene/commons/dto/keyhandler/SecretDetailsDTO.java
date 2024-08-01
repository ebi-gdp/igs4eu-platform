package uk.ac.ebi.gdp.intervene.commons.dto.keyhandler;

public class SecretDetailsDTO {
    private final String secretId;
    private final String secretIdVersion;

    public SecretDetailsDTO(final String secretId,
                            final String secretIdVersion) {
        this.secretId = secretId;
        this.secretIdVersion = secretIdVersion;
    }

    public String getSecretId() {
        return secretId;
    }

    public String getSecretIdVersion() {
        return secretIdVersion;
    }
}
