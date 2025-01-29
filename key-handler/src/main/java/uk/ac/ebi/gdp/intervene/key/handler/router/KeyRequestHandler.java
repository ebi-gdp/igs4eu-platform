/*
 *
 * Copyright 2024 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package uk.ac.ebi.gdp.intervene.key.handler.router;

import com.google.api.gax.rpc.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.commons.dto.keyhandler.DatasetCryptographyDetailsDTO;
import uk.ac.ebi.gdp.intervene.commons.dto.keyhandler.SecretDetailsDTO;
import uk.ac.ebi.gdp.intervene.cryptography.aes.AESCryptography;
import uk.ac.ebi.gdp.intervene.key.handler.dto.PrivateKeyDetailsDTO;
import uk.ac.ebi.gdp.intervene.key.handler.secret.ISecretManager;
import uk.ac.ebi.gdp.intervene.key.handler.service.Crypt4ghKeyGenerator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.notExists;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.readString;
import static java.nio.file.Path.of;
import static java.util.UUID.randomUUID;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;
import static org.springframework.web.reactive.function.server.ServerResponse.status;
import static reactor.core.publisher.Mono.fromCallable;
import static reactor.core.scheduler.Schedulers.boundedElastic;
import static uk.ac.ebi.gdp.intervene.commons.exception.ClientException.badRequest;
import static uk.ac.ebi.gdp.intervene.commons.exception.ClientException.resourceNotFound;
import static uk.ac.ebi.gdp.intervene.commons.exception.ServerException.serverException;
import static uk.ac.ebi.gdp.intervene.key.handler.service.IKeyGenerator.KeyGeneratorStatus.SUCCESS;

/**
 * The handler function that processes requests.
 */
public class KeyRequestHandler {
    private final Logger LOGGER = LoggerFactory.getLogger(KeyRequestHandler.class);
    private final Crypt4ghKeyGenerator crypt4ghKeyGenerator;
    private final ISecretManager secretManager;
    private final Path keysBasePath;
    private final char[] privateKeyPassword;
    private final AESCryptography aesCryptography;
    private final Path secretPathPrefix;

    /**
     * Constructs a new KeyRequestHandler with the specified Crypt4ghKeyGenerator,
     * ISecretManager, base path for keys, and private key password.
     *
     * @param crypt4ghKeyGenerator the Crypt4ghKeyGenerator used for generating keys.
     * @param secretManager the ISecretManager instance for managing secrets.
     * @param keysBasePath the base path where the keys are stored.
     * @param privateKeyPassword the password for the private key as a character array.
     * @param secretPathPrefix gcp secret path prefix.
     */
    public KeyRequestHandler(final Crypt4ghKeyGenerator crypt4ghKeyGenerator,
                             final ISecretManager secretManager,
                             final Path keysBasePath,
                             final char[] privateKeyPassword,
                             final Path secretPathPrefix) {
        this.crypt4ghKeyGenerator = crypt4ghKeyGenerator;
        this.secretManager = secretManager;
        this.keysBasePath = keysBasePath;
        this.privateKeyPassword = privateKeyPassword;
        this.secretPathPrefix = secretPathPrefix;
        this.aesCryptography = new AESCryptography.Builder().build();
    }

    /**
     * Generate private/public key pair.
     *
     * @return a Mono emitting the ServerResponse containing {@link DatasetCryptographyDetailsDTO}.
     */
    public Mono<ServerResponse> generateKeys() {
        final String randomUUID = generateRandomUUID();
        final Path privateKeyPath = keysBasePath.resolve(randomUUID + ".sec");
        final Path publicKeyPath = keysBasePath.resolve(randomUUID + ".pub");
        return crypt4ghKeyGenerator
                .generate(privateKeyPath, publicKeyPath)
                .doOnNext(keyGeneratorStatus -> validateFileKeyPairs(privateKeyPath, publicKeyPath))
                .flatMap(keyGeneratorStatus -> {
                    if (keyGeneratorStatus == SUCCESS) {
                        return readFileContentAsBytes(privateKeyPath)
                                .map(privateKeyBytes -> aesCryptography.encrypt(privateKeyBytes, privateKeyPassword))
                                .flatMap(encryptedPrivateKey -> uploadPrivateKeyOnSecretManager(randomUUID, encryptedPrivateKey.getBytes()))
                                .flatMap(secretDetailsDTO -> ok()
                                        .bodyValue(buildDatasetCryptographyDetailsDTO(secretDetailsDTO, publicKeyPath)))
                                .doOnNext(secretIdVersionNotInUse -> deleteFiles(List.of(privateKeyPath, publicKeyPath)));
                    } else {
                        return status(INTERNAL_SERVER_ERROR)
                                .build();
                    }
                });
    }

    private DatasetCryptographyDetailsDTO buildDatasetCryptographyDetailsDTO(final SecretDetailsDTO secretDetailsDTO,
                                                                             final Path publicKeyPath) {
        return new DatasetCryptographyDetailsDTO(
                secretDetailsDTO.getSecretId(),
                secretDetailsDTO.getSecretIdVersion(),
                readFileContentAsString(publicKeyPath),
                secretDetailsDTO.getExpiresAt());
    }

    private void validateFileKeyPairs(final Path privateKeyPath,
                                      final Path publicKeyPath) {
        if (notExists(privateKeyPath)) {
            throw serverException("Private key not found at path: " + privateKeyPath);
        }

        if (notExists(publicKeyPath)) {
            throw serverException("Public key not found at path: " + publicKeyPath);
        }
    }

    private Mono<byte[]> readFileContentAsBytes(final Path privateKeyPath) {
        return fromCallable(() -> {
            try {
                return readAllBytes(privateKeyPath);
            } catch (IOException e) {
                throw serverException("Error reading file");
            }
        }).subscribeOn(boundedElastic());
    }

    private Mono<SecretDetailsDTO> uploadPrivateKeyOnSecretManager(final String secretId,
                                                                   final byte[] privateKeyEncryptedContent) {
        try {
            return secretManager.uploadSecret(secretId,
                    new ByteArrayInputStream(privateKeyEncryptedContent));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw serverException("Error while uploading encrypted private key: " + e.getMessage());
        }
    }

    private String generateRandomUUID() {
        return randomUUID()
                .toString()
                .toUpperCase();
    }

    private String readFileContentAsString(final Path filePath) {
        try {
            return readString(filePath);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw serverException("Error while uploading encrypted private key: " + e.getMessage());
        }
    }

    /**
     * Handles an HTTP request to retrieve a private key.
     *
     * @param serverRequest the request containing the parameters for retrieving the private key.
     *
     * @return a Mono emitting the ServerResponse containing the private key or an error status.
     */
    public Mono<ServerResponse> retrievePrivateKey(final ServerRequest serverRequest) {
        final String secretId = serverRequest.pathVariable("keyId");
        final String versionId = serverRequest.pathVariable("versionId");
        return downloadSecret(secretId, of(versionId).getFileName().toString())
                .map(String::new)
                .flatMap(privateKeyContent -> ok()
                        .bodyValue(new PrivateKeyDetailsDTO(privateKeyContent)));
    }

    private Mono<byte[]> downloadSecret(final String secretId,
                                        final String secretVersionId) {
        try {
            return secretManager
                    .downloadSecret(secretId, secretVersionId)
                    .flatMap(inputStream -> fromCallable(inputStream::readAllBytes))
                    .subscribeOn(boundedElastic());
        } catch (NotFoundException nfe) {
            LOGGER.error(nfe.getMessage(), nfe);
            throw resourceNotFound("Specified Secret ID: %s and Version ID: %s not found!".formatted(secretId, secretVersionId));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw serverException("Error while downloading encrypted private key: " + e.getMessage());
        }
    }

    private void deleteFiles(final List<Path> paths) {
        paths.forEach(path -> {
            try {
                deleteIfExists(path);
                LOGGER.info("File {} is successfully deleted!", path);
            } catch (NotFoundException nfe) {
                LOGGER.error("Specified path: {} not found!", path, nfe);
            } catch (IOException e) {
                LOGGER.error("Error while deleting key: {}.", path, e);
            }
        });
    }

    /**
     * @param serverRequest the request containing the parameters for deleting the private key.
     *
     * @return a Mono emitting the ServerResponse containing http status. e.g. 200 for successful
     *         request & other http statuses incase any error occurs.
     */
    public Mono<ServerResponse> deleteSecret(final ServerRequest serverRequest) {
        final String secretId = serverRequest.queryParam("secretId")
                .orElseThrow(() -> badRequest("Query param 'secretId' is missing!"));
        try {
            return secretManager
                    .deleteSecret(secretPathPrefix.resolve(secretId).toString())
                    .then(ok().build());
        } catch (NotFoundException nfe) {
            LOGGER.error(nfe.getMessage(), nfe);
            throw resourceNotFound("Specified secretId: %s not found!".formatted(secretId));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw serverException("Error while deleting encrypted private key: " + e.getMessage());
        }
    }
}
