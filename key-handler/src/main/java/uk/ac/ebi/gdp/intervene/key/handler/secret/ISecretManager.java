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
package uk.ac.ebi.gdp.intervene.key.handler.secret;

import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;

/**
 * Interface for managing secrets in a secret management system.
 * This interface defines operations for uploading and downloading secrets.
 */
public interface ISecretManager {

    /**
     * Uploads a secret to the secret manager.
     *
     * @param secretId the identifier of the secret to be uploaded.
     * @param secretContent the content of the secret as an InputStream.
     *
     * @return a Mono emitting the version number of the uploaded secret as a String.
     * @throws IOException if an I/O error occurs during the secret upload process.
     */
    Mono<String> uploadSecret(String secretId,
                              InputStream secretContent) throws IOException;

    /**
     * Downloads a secret from the secret manager.
     *
     * @param secretId the identifier of the secret to be downloaded.
     * @param secretVersion the version of the secret to be downloaded.
     *
     * @return a Mono emitting the content of the secret as an InputStream.
     * @throws IOException if an I/O error occurs during the secret download process.
     */
    Mono<InputStream> downloadSecret(String secretId,
                                     String secretVersion) throws IOException;
}

