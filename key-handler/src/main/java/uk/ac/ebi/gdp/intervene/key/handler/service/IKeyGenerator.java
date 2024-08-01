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
package uk.ac.ebi.gdp.intervene.key.handler.service;

import reactor.core.publisher.Mono;

import java.nio.file.Path;

/**
 * Interface representing a key generator that can generate cryptographic key pairs.
 */
public interface IKeyGenerator {

    /**
     * Generates a new key pair and saves them to the specified paths.
     *
     * @param privateKeyPath the path where the private key will be saved.
     * @param publicKeyPath the path where the public key will be saved.
     * @return a Mono emitting the status of the key generation process as a KeyGeneratorStatus.
     * @throws Exception if an error occurs during the key generation process.
     */
    Mono<KeyGeneratorStatus> generate(Path privateKeyPath,
                                      Path publicKeyPath) throws Exception;

    enum KeyGeneratorStatus {
        SUCCESS, FAILURE
    }
}
