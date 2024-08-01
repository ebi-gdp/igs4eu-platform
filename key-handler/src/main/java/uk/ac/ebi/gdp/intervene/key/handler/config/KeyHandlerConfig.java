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
package uk.ac.ebi.gdp.intervene.key.handler.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.gdp.intervene.commons.dto.constant.GCPRegion;
import uk.ac.ebi.gdp.intervene.key.handler.cryptography.Crypt4ghKeygen;
import uk.ac.ebi.gdp.intervene.key.handler.router.KeyRequestHandler;
import uk.ac.ebi.gdp.intervene.key.handler.secret.ISecretManager;
import uk.ac.ebi.gdp.intervene.key.handler.secret.SecretConfig;
import uk.ac.ebi.gdp.intervene.key.handler.secret.gcp.GCPSecretManager;
import uk.ac.ebi.gdp.intervene.key.handler.service.Crypt4ghKeyGenerator;

import java.nio.file.Path;
import java.util.List;

/**
 * Configuration class for setting up beans related to key handling and GCP Secret Manager.
 */
@Configuration
public class KeyHandlerConfig {

    /**
     * Creates a SecretConfig bean with properties prefixed with "gcp.secret-manager.config".
     *
     * @return a new instance of SecretConfig.
     */
    @ConfigurationProperties(prefix = "gcp.secret-manager.config")
    @Bean
    public SecretConfig secretConfig() {
        return new SecretConfig();
    }

    /**
     * Creates an ISecretManager bean configured for GCP Secret Manager.
     *
     * @param projectId the GCP project ID, injected from the application properties.
     * @param gcpRegion the GCP region, injected from the application properties.
     * @param secretConfig the configuration for the secret manager.
     *
     * @return a new instance of GCPSecretManager.
     */
    @Bean
    public ISecretManager GCPSecretManager(@Value("${gcp.project-id}") final String projectId,
                                           @Value("${gcp.region}") final GCPRegion gcpRegion,
                                           SecretConfig secretConfig) {
        return new GCPSecretManager(projectId, gcpRegion, secretConfig);
    }

    /**
     * Creates a Crypt4ghKeygen bean for generating Crypt4GH keys.
     *
     * @param crypt4ghKeygen the path to the Crypt4GH keygen binary, injected from the application properties.
     *
     * @return a new instance of Crypt4ghKeygen.
     */
    @Bean
    public Crypt4ghKeygen crypt4ghKeygen(@Value("${crypt4gh.binary-path}") final Path crypt4ghKeygen) {
        return Crypt4ghKeygen
                .builder(crypt4ghKeygen)
                .build();
    }

    /**
     * Creates a Crypt4ghKeyGenerator bean for generating keys using Crypt4ghKeygen.
     *
     * @param crypt4ghKeygen an instance of Crypt4ghKeygen.
     * @param shellInterpreterCmds the shell interpreter commands, injected from the application properties.
     *
     * @return a new instance of Crypt4ghKeyGenerator.
     */
    @Bean
    public Crypt4ghKeyGenerator crypt4ghKeyGenerator(final Crypt4ghKeygen crypt4ghKeygen,
                                                     @Value("#{'${crypt4gh.shell-path}'.split(' ')}") final List<String> shellInterpreterCmds) {
        return new Crypt4ghKeyGenerator(crypt4ghKeygen, shellInterpreterCmds);
    }

    /**
     * Creates a KeyRequestHandler bean for handling key requests.
     *
     * @param crypt4ghKeyGenerator an instance of Crypt4ghKeyGenerator.
     * @param secretManager an instance of ISecretManager.
     * @param keysBasePath the base path for keys, injected from the application properties.
     * @param privateKeyPassword the password for the private key, injected from the application properties.
     *
     * @return a new instance of KeyRequestHandler.
     */
    @Bean
    public KeyRequestHandler keyRequestHandler(final Crypt4ghKeyGenerator crypt4ghKeyGenerator,
                                               final ISecretManager secretManager,
                                               @Value("${crypt4gh.keys.base-path}") final Path keysBasePath,
                                               @Value("${crypt4gh.private-key.password}") final String privateKeyPassword) {
        return new KeyRequestHandler(crypt4ghKeyGenerator,
                secretManager,
                keysBasePath,
                privateKeyPassword.toCharArray());
    }
}
