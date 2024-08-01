package uk.ac.ebi.gdp.intervene.key.handler.cryptography;

import java.nio.file.Path;
import java.util.StringJoiner;

import static java.util.Objects.requireNonNull;

/**
 * Crypt4ghKeygen is a utility class for generating cryptographic keys using the Crypt4GH tool.
 * It supports generating both private and public keys and includes an optional passphrase for
 * the private key.
 */
public class Crypt4ghKeygen {
    private final Path crypt4ghKeygenBinAbsolutePath;
    private final char[] crypt4ghPrivateKeyPassphrase;

    /**
     * Private constructor for the Crypt4ghKeygen class, invoked by the Builder.
     *
     * @param builder The Builder instance used to construct the Crypt4ghKeygen object.
     */
    private Crypt4ghKeygen(final Builder builder) {
        this.crypt4ghKeygenBinAbsolutePath = builder.crypt4ghKeygenBinAbsolutePath;
        this.crypt4ghPrivateKeyPassphrase = builder.crypt4ghPrivateKeyPassphrase;
    }

    /**
     * Creates a Builder instance for constructing a Crypt4ghKeygen object.
     *
     * @param crypt4ghKeygenBinAbsolutePath The absolute path to the Crypt4GH keygen binary.
     *
     * @return A Builder instance for constructing a Crypt4ghKeygen object.
     */
    public static Builder builder(final Path crypt4ghKeygenBinAbsolutePath) {
        return new Builder(crypt4ghKeygenBinAbsolutePath);
    }

    /**
     * Generates a bash command string for creating Crypt4GH keys.
     *
     * @param crypt4ghPrivateKeyAbsolutePath The absolute path where the private key will be saved.
     * @param crypt4ghPublicKeyAbsolutePath The absolute path where the public key will be saved.
     *
     * @return A bash command string to generate Crypt4GH keys.
     */
    public String crypt4ghGenerateKeysBashCmd(final Path crypt4ghPrivateKeyAbsolutePath,
                                              final Path crypt4ghPublicKeyAbsolutePath) {
        requireNonNull(crypt4ghPrivateKeyAbsolutePath, "Crypt4gh private key absolute path cannot be null");
        requireNonNull(crypt4ghPublicKeyAbsolutePath, "Crypt4gh public key absolute path cannot be null");

        final String passphrase = crypt4ghPrivateKeyPassphrase.length == 0 ? "" : new String(crypt4ghPrivateKeyPassphrase);

        return new StringJoiner(" ")
                .add("'/usr/bin/expect")
                .add("-c")
                .add("\"spawn")
                .add(crypt4ghKeygenBinAbsolutePath.toString())
                .add("--sk")
                .add(crypt4ghPrivateKeyAbsolutePath.toString())
                .add("--pk")
                .add(crypt4ghPublicKeyAbsolutePath.toString())
                .add("; expect \\\"Enter passphrase for %s (empty for no passphrase):\\\"".formatted(crypt4ghPrivateKeyAbsolutePath.toString()))
                .add("; send \\\"" + passphrase + "\\r\\\"")
                .add("; expect \\\"Enter passphrase for %s (again):\\\"".formatted(crypt4ghPrivateKeyAbsolutePath.toString()))
                .add("; send \\\"" + passphrase + "\\r\\\"")
                .add("; interact\"'")
                .toString();
    }

    /**
     * Builder class for constructing Crypt4ghKeygen objects.
     */
    public static class Builder {
        private final Path crypt4ghKeygenBinAbsolutePath;
        private char[] crypt4ghPrivateKeyPassphrase = new char[0]; // Default empty passphrase

        /**
         * Private constructor for the Builder class.
         *
         * @param crypt4ghKeygenBinAbsolutePath The absolute path to the Crypt4GH keygen binary.
         */
        private Builder(final Path crypt4ghKeygenBinAbsolutePath) {
            this.crypt4ghKeygenBinAbsolutePath = requireNonNull(crypt4ghKeygenBinAbsolutePath, "Crypt4gh keygen bin absolute path cannot be null");
        }

        /**
         * Sets the optional passphrase for the private key.
         *
         * @param crypt4ghPrivateKeyPassphrase The passphrase for the private key.
         *
         * @return The Builder instance.
         */
        public Builder withCrypt4ghPrivateKeyPassphrase(final char[] crypt4ghPrivateKeyPassphrase) {
            this.crypt4ghPrivateKeyPassphrase = crypt4ghPrivateKeyPassphrase != null ? crypt4ghPrivateKeyPassphrase.clone() : new char[0];
            return this;
        }

        /**
         * Builds and returns a Crypt4ghKeygen object.
         *
         * @return A Crypt4ghKeygen object.
         */
        public Crypt4ghKeygen build() {
            return new Crypt4ghKeygen(this);
        }
    }
}





