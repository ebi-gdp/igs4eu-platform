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
package uk.ac.ebi.gdp.intervene.key.handler.secret.gcp;

import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.AddSecretVersionRequest;
import com.google.cloud.secretmanager.v1.CreateSecretRequest;
import com.google.cloud.secretmanager.v1.Replication;
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretPayload;
import com.google.cloud.secretmanager.v1.SecretVersion;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.google.protobuf.Duration;
import com.google.protobuf.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.commons.dto.constant.GCPRegion;
import uk.ac.ebi.gdp.intervene.commons.dto.keyhandler.SecretDetailsDTO;
import uk.ac.ebi.gdp.intervene.key.handler.secret.ISecretManager;
import uk.ac.ebi.gdp.intervene.key.handler.secret.SecretConfig;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;

import static com.google.cloud.secretmanager.v1.SecretManagerServiceClient.create;
import static com.google.protobuf.ByteString.readFrom;
import static java.time.Instant.ofEpochSecond;
import static java.time.LocalDateTime.ofInstant;
import static java.time.ZoneId.systemDefault;
import static reactor.core.publisher.Mono.just;

/**
 * A manager class for handling secrets in Google Cloud Platform (GCP) Secret Manager.
 * Implements the {@link ISecretManager} interface.
 */
public class GCPSecretManager implements ISecretManager {
    private final Logger LOGGER = LoggerFactory.getLogger(GCPSecretManager.class);
    private final String projectId;
    private final GCPRegion gcpRegion;
    private final SecretConfig secretConfig;

    /**
     * Constructs a new GCPSecretManager with the specified project ID, GCP region, and secret configuration.
     *
     * @param projectId the ID of the GCP project.
     * @param gcpRegion the GCP region where the secrets are stored.
     * @param secretConfig the configuration settings for secrets.
     */
    public GCPSecretManager(final String projectId,
                            final GCPRegion gcpRegion,
                            final SecretConfig secretConfig) {
        this.projectId = projectId;
        this.gcpRegion = gcpRegion;
        this.secretConfig = secretConfig;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<SecretDetailsDTO> uploadSecret(final String secretId,
                                               final InputStream secretContent) throws IOException {
        // Initialize client that will be used to send requests.
        try (final SecretManagerServiceClient smsClient = create()) {
            // Build the parent name from the project.
            final Secret createdSecret = smsClient
                    .createSecret(buildCreateSecretRequest(secretId));

            // Build secret version request
            final AddSecretVersionRequest request = AddSecretVersionRequest
                    .newBuilder()
                    .setParent(createdSecret.getName())
                    .setPayload(buildSecretPayload(secretContent))
                    .build();

            // Add secret version
            final SecretVersion addedVersion = smsClient
                    .addSecretVersion(request);
            return just(new SecretDetailsDTO(
                    secretId,
                    addedVersion.getName(),
                    timestampToLocalDateTime(createdSecret.getExpireTime())));
        }
    }

    private Replication buildReplicationPolicy() {
        // Create the replication policy
        final Replication.UserManaged.Replica replica = Replication
                .UserManaged.Replica
                .newBuilder()
                .setLocation(gcpRegion.getRegionName())
                .build();
        final Replication.UserManaged userManaged = Replication.UserManaged
                .newBuilder()
                .addReplicas(replica)
                .build();
        return Replication
                .newBuilder()
                .setUserManaged(userManaged)
                .build();
    }

    private CreateSecretRequest buildCreateSecretRequest(final String secretId) {
        LOGGER.debug("Secret TTL value: {}", secretConfig.getTtl());
        // Create the parent secret.
        final Secret secret = Secret
                .newBuilder()
                .setReplication(buildReplicationPolicy())
                .setTtl(Duration.newBuilder()
                        .setSeconds(secretConfig.getTtl())
                        .build())
                .build();
        return CreateSecretRequest
                .newBuilder()
                .setParent("projects/" + projectId)
                .setSecretId(secretId)
                .setSecret(secret)
                .build();
    }

    private SecretPayload buildSecretPayload(final InputStream secretContent) throws IOException {
        return SecretPayload
                .newBuilder()
                .setData(readFrom(secretContent))
                .build();
    }

    public LocalDateTime timestampToLocalDateTime(final Timestamp timestamp) {
        // Convert Timestamp to Instant
        final Instant instant = ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());

        // Convert Instant to LocalDateTime
        return ofInstant(instant, systemDefault());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<InputStream> downloadSecret(final String secretId,
                                            final String secretVersion) throws IOException {
        // Initialize client that will be used to send requests.
        try (final SecretManagerServiceClient smsClient = create()) {
            // Build the resource name of the secret version.
            final SecretVersionName secretVersionName = SecretVersionName.of(
                    projectId,
                    secretId,
                    secretVersion
            );

            // Access the secret version.
            final AccessSecretVersionResponse response = smsClient.accessSecretVersion(secretVersionName);

            // Get the payload of the secret as InputStream.
            final InputStream inputStream = response
                    .getPayload()
                    .getData()
                    .newInput();
            return just(inputStream);
        }
    }
}
