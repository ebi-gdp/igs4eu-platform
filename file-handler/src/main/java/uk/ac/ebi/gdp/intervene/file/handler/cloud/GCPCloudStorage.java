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
package uk.ac.ebi.gdp.intervene.file.handler.cloud;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.InputStreamSource;
import reactor.core.publisher.Mono;

import java.nio.channels.Channels;
import java.util.stream.Stream;

import static uk.ac.ebi.gdp.intervene.commons.exception.ClientException.badRequest;
import static uk.ac.ebi.gdp.intervene.commons.exception.ClientException.resourceNotFound;

/**
 * Google Cloud Storage implementation.
 */
public class GCPCloudStorage implements ICloudStorage {
    private final Storage storage;

    /**
     * Constructs a {@code GCPCloudStorage} instance.
     * This service provides operations for interacting with Google Cloud Storage.
     *
     * @param storage the Google Cloud Storage client used to perform storage operations.
     */
    public GCPCloudStorage(final Storage storage) {
        this.storage = storage;
    }

    /**
     * List files on GCP storage.
     * {@inheritDoc}
     */
    @Override
    public Mono<Stream<String>> listFiles(final String bucketName,
                                          final String pathPrefix) {
        try {
            final Page<Blob> blobs = storage
                    .list(bucketName.toLowerCase(), Storage.BlobListOption.prefix(pathPrefix));
            return Mono.just(blobs
                    .streamValues()
                    .map(BlobInfo::getName)
                    .filter(ICloudStorage::isValidFileForDownload)
                    .parallel());
        } catch (StorageException se) {
            if (se.getCause() instanceof GoogleJsonResponseException) {
                return Mono.error(resourceNotFound("File(s) not found! bucketName: %s, pathPrefix: %s".formatted(bucketName, pathPrefix)));
            }
            return Mono.error(se);
        }
    }

    /**
     * Stream file on GCP storage.
     * {@inheritDoc}
     */
    @Override
    public Mono<InputStreamSource> streamFileFromBucket(final String bucketName,
                                                        final String path) {
        final Blob blob = storage.get(BlobId.of(bucketName.toLowerCase(), path));
        if (blob == null) {
            return Mono.error(badRequest("Path not found on Bucket! bucketName: %s, path: %s".formatted(bucketName, path)));
        } else {
            return Mono.just(new InputStreamResource(Channels
                    .newInputStream(blob.reader())));
        }
    }
}
