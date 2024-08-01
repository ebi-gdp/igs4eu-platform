package uk.ac.ebi.gdp.intervene.commons.dto.keyhandler;

public class PublicKeyDetailsDTO extends SecretDetailsDTO {
    private final String publicKey;

    public PublicKeyDetailsDTO(final String secretId,
                               final String secretIdVersion,
                               final String publicKey) {
        super(secretId, secretIdVersion);
        this.publicKey = publicKey;
    }

    public String getPublicKey() {
        return publicKey;
    }
}
