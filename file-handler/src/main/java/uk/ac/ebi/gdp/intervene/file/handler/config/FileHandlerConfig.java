/*
 *
 * Copyright 2021 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.gdp.intervene.file.handler.config;

import com.amazonaws.services.s3.AmazonS3;
import com.google.cloud.storage.StorageOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import uk.ac.ebi.gdp.file.handler.core.properties.WebClientProperties;
import uk.ac.ebi.gdp.intervene.commons.dpa.DPAConsentCheck;
import uk.ac.ebi.gdp.intervene.commons.dpa.IUserManagerService;
import uk.ac.ebi.gdp.intervene.commons.dpa.DefaultUserManagerService;
import uk.ac.ebi.gdp.intervene.commons.exception.ReactiveExceptionHandler;
import uk.ac.ebi.gdp.intervene.commons.openapi.OpenAPIConfig;
import uk.ac.ebi.gdp.intervene.commons.utility.WebClientUtil;
import uk.ac.ebi.gdp.intervene.file.handler.cloud.GCPCloudStorage;
import uk.ac.ebi.gdp.intervene.file.handler.cloud.ICloudStorage;
import uk.ac.ebi.gdp.intervene.file.handler.cloud.S3CloudStorage;
import uk.ac.ebi.gdp.intervene.file.handler.service.ega.EGAFileService;

import java.net.URI;

import static uk.ac.ebi.gdp.intervene.commons.constants.PlatformConstants.PIPELINE_EXECUTION_PLATFORM;
import static uk.ac.ebi.gdp.intervene.commons.constants.PlatformType.CSC;
import static uk.ac.ebi.gdp.intervene.commons.constants.PlatformType.GCP;

/**
 * Bean config for file handler service.
 *
 * @see ReactiveExceptionHandler
 * @see WebClient
 * @see RetryTemplate
 * @see WebClientProperties
 * @see EGAFileService
 */
@Import({ReactiveExceptionHandler.class, OpenAPIConfig.class})
@Configuration
public class FileHandlerConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileHandlerConfig.class);

    @Bean
    public EGAFileService egaFileService(@Qualifier("egaWebClient") final WebClient webClient,
                                         final RetryTemplate retryTemplate,
                                         final WebClientProperties webClientProperties,
                                         @Value("${ega.file.storage.path}") final String storagePath) {
        final int IV = 16 + 1;
        return new EGAFileService(
                webClient,
                retryTemplate,
                storagePath,
                webClientProperties.getPipeSize(),
                IV
        );
    }

    /**
     * Creates {@link DPAConsentCheck} bean for checking user consent status on DPA.
     *
     * @param userManagerService the {@link DefaultUserManagerService} default implementation.
     *
     * @return {@link DPAConsentCheck} instance.
     */
    @Bean
    public DPAConsentCheck dpaConsentCheck(final IUserManagerService userManagerService) {
        return new DPAConsentCheck(userManagerService);
    }

    /**
     * User manager service instance
     *
     * @param userManagerWebClient user manager web client
     * @param userAccountURI user account URI
     *
     * @return UserManager service instance
     */
    @Bean
    public IUserManagerService userManagerService(@Qualifier("userManagerWebClient") final WebClient userManagerWebClient,
                                                  @Value("${intervene.user-manager.user-account.uri}") final URI userAccountURI) {
        return new DefaultUserManagerService(userManagerWebClient, userAccountURI);
    }

    /**
     * WebClient bean for user manager
     *
     * @param baseURL user manager service baseURL
     *
     * @return {@link WebClient}
     */
    @Bean("userManagerWebClient")
    public WebClient webClient(@Value("${intervene.user-manager.base-url}") final String baseURL) {
        return WebClientUtil.webClient(baseURL, LOGGER);
    }

    /**
     * Creates an {@link ICloudStorage} bean configured for Google Cloud Storage (GCS).
     * This bean is only created if the property {@code pipeline.execution.platform} is set to {@code GCP}.
     *
     * @param gcpProjectId the Google Cloud Project ID to use for GCS operations.
     *
     * @return an {@link ICloudStorage} instance configured to use GCS.
     */
    @ConditionalOnProperty(value = PIPELINE_EXECUTION_PLATFORM, havingValue = GCP)
    @Bean
    public ICloudStorage gcpCloudStorage(@Value("${cloud.gcp.project-id}") final String gcpProjectId) {
        return new GCPCloudStorage(
                StorageOptions.newBuilder().setProjectId(gcpProjectId).build().getService());
    }

    /**
     * Creates an {@link ICloudStorage} bean configured for S3 cloud storage.
     * This bean is only created if the property {@code PIPELINE_EXECUTION_PLATFORM} is set to {@code CSC}.
     *
     * @param amazonS3 the {@link AmazonS3} client instance used for S3 operations.
     *
     * @return an {@link ICloudStorage} instance configured to use S3.
     */
    @ConditionalOnProperty(value = PIPELINE_EXECUTION_PLATFORM, havingValue = CSC)
    @Bean
    public ICloudStorage s3CloudStorage(final AmazonS3 amazonS3) {
        return new S3CloudStorage(amazonS3);
    }
}
