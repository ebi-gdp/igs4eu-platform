/*
 *
 * Copyright 2022 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.gdp.intervene.pipeline.manager.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.storage.StorageOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.oauth2.server.resource.web.reactive.function.client.ServerBearerExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import uk.ac.ebi.gdp.intervene.commons.dpa.DPAConsentCheck;
import uk.ac.ebi.gdp.intervene.commons.dpa.IUserManagerService;
import uk.ac.ebi.gdp.intervene.commons.dto.filehandler.IGlobusFileDetailsWrapper;
import uk.ac.ebi.gdp.intervene.commons.exception.ReactiveExceptionHandler;
import uk.ac.ebi.gdp.intervene.commons.utility.CommonUtil;
import uk.ac.ebi.gdp.intervene.pipeline.manager.message.TriggerPipelineEvent;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.GlobusDetailsRepository;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.GlobusUserRepository;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.PipelineDetailsRepository;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.PipelineExecutionStatusRepository;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.PipelineResultRepository;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.service.IPipelinePersistence;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.service.PipelinePersistence;
import uk.ac.ebi.gdp.intervene.pipeline.manager.router.validation.FileValidations;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.GCPCloudStorage;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.GlobusFileHandlerService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.GlobusManagerService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.ICloudStorage;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.KeyHandlerService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.PGSCatalogService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.PipelineManagerService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.S3CloudStorage;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.UserManagerService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.message.HttpMessageService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.message.KafkaMessageService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.message.MessageService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.message.S3MessageService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.utility.EmailSender;
import uk.ac.ebi.gdp.intervene.pipeline.manager.utility.IEmailSender;

import java.net.URI;

import static uk.ac.ebi.gdp.intervene.commons.log.LogUtil.propagateRequestId;
import static uk.ac.ebi.gdp.intervene.commons.utility.WebClientUtil.jsonExchangeStrategies;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.constant.PlatformType.CSC;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.constant.PlatformType.EBI_EMBASSY;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.constant.PlatformType.GCP;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.constant.PlatformType.HTTP;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.constant.PlatformType.KAFKA;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.constant.PlatformType.S3;

/**
 * Bean config for pipeline manager service.
 */
@Import(ReactiveExceptionHandler.class)
@Configuration
public class PipelineManagerConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(PipelineManagerConfig.class);
    private static final String PIPELINE_EXECUTION_PLATFORM = "pipeline-execution.platform";
    public static final String PIPELINE_REQUEST_MODE = "pipeline-request.mode";

    @Bean
    public IPipelinePersistence pipelinePersistence(final PipelineDetailsRepository pipelineDetailsRepository,
                                                    final PipelineExecutionStatusRepository pipelineExecutionStatusRepository,
                                                    final PipelineResultRepository pipelineResultRepository) {
        return new PipelinePersistence(
                pipelineDetailsRepository,
                pipelineExecutionStatusRepository,
                pipelineResultRepository);
    }

    /**
     * Creates an AmazonS3 bean configured for the Allas S3 storage service.
     * This bean is only created if the property {@code PIPELINE_EXECUTION_PLATFORM} is set to {@code CSC}.
     *
     * @param endpoint the S3 endpoint URL.
     * @param accessKey the access key for S3 credentials.
     * @param secretKey the secret key for S3 credentials.
     * @param region the region of the S3 service.
     *
     * @return a configured {@code AmazonS3} instance for interacting with the Allas S3 storage service
     */
    @ConditionalOnProperty(value = PIPELINE_EXECUTION_PLATFORM, havingValue = CSC)
    @Bean("allasS3")
    public AmazonS3 allasS3(@Value("${allas.s3.endpoint}") final String endpoint,
                            @Value("${allas.s3.credentials.access-key}") final String accessKey,
                            @Value("${allas.s3.credentials.secret-key}") final String secretKey,
                            @Value("${allas.s3.credentials.region}") final String region) {
        return amazonS3(
                endpoint,
                accessKey,
                secretKey,
                region
        );
    }

    /**
     * Creates an AmazonS3 bean configured for the EBI Embassy S3 storage service.
     * This bean is only created if the property {@code PIPELINE_EXECUTION_PLATFORM} is set to {@code EBI_EMBASSY}.
     *
     * @param endpoint the S3 endpoint URL for the EBI Embassy service.
     * @param accessKey the access key for EBI Embassy S3 credentials.
     * @param secretKey the secret key for EBI Embassy S3 credentials.
     * @param region the AWS region for the EBI Embassy S3 service.
     *
     * @return a configured {@code AmazonS3} instance for interacting with the EBI Embassy S3 storage service.
     */
    @ConditionalOnProperty(value = PIPELINE_EXECUTION_PLATFORM, havingValue = EBI_EMBASSY)
    @Bean("embassyS3")
    public AmazonS3 embassyS3(@Value("${ebi-embassy.s3.endpoint}") final String endpoint,
                              @Value("${ebi-embassy.s3.credentials.access-key}") final String accessKey,
                              @Value("${ebi-embassy.s3.credentials.secret-key}") final String secretKey,
                              @Value("${ebi-embassy.s3.credentials.region}") final String region) {
        return amazonS3(
                endpoint,
                accessKey,
                secretKey,
                region
        );
    }

    /**
     * Creates a {@link MessageService} bean configured for S3 messaging.
     * This bean is only created if the property {@code PIPELINE_REQUEST_MODE} is set to {@code S3}.
     *
     * @param s3Client the {@link AmazonS3} client instance used for S3 operations.
     * @param bucketName the format for the cloud storage bucket name.
     *
     * @return a {@link MessageService} instance configured to use S3 for messaging.
     */
    @ConditionalOnProperty(value = PIPELINE_REQUEST_MODE, havingValue = S3)
    @Bean("s3MssageService")
    public MessageService s3MessageService(@Qualifier("allasS3") final AmazonS3 s3Client,
                                           @Value("${cloud.storage.bucket-name-format}") final String bucketName) {
        return new S3MessageService(s3Client, bucketName);
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

    /**
     * Configuration class for setting up HTTP-based message services.
     * This configuration class is conditionally loaded when the property `PIPELINE_REQUEST_MODE` has the value `HTTP`.
     * It provides beans for HTTP message services and the corresponding `WebClient` configured for pipeline requests.
     */
    @ConditionalOnProperty(value = PIPELINE_REQUEST_MODE, havingValue = HTTP)
    @Configuration
    public static class HttpMessageServiceConfig {
        /**
         * Creates a {@link MessageService} bean for handling HTTP-based message requests.
         * This bean is configured with a {@link WebClient} and a URI for pipeline request endpoints.
         *
         * @param webClient the {@link WebClient} instance used for making HTTP requests, injected by Spring.
         * @param pipelineRequestURI the URI for the pipeline request service, injected from configuration properties.
         *
         * @return a {@link MessageService} instance configured with the provided {@link WebClient} and URI.
         */
        @Bean("httpMessageService")
        public MessageService httpMessageService(@Qualifier("pipelineRequestWebClient") final WebClient webClient,
                                                 @Value("${intervene.pipeline-request.uri}") final URI pipelineRequestURI) {
            return new HttpMessageService(webClient, pipelineRequestURI);
        }

        /**
         * Creates a {@link WebClient} bean configured for making HTTP requests to the pipeline request service.
         * This {@link WebClient} is set up with the base URL specified in the configuration and uses custom exchange strategies.
         *
         * @param baseURL the base URL for the pipeline request service, injected from configuration properties.
         *
         * @return a {@link WebClient} instance configured with the provided base URL and custom exchange strategies.
         */
        @Bean("pipelineRequestWebClient")
        public WebClient webClientBasic(@Value("${intervene.pipeline-request.base-url}") final String baseURL) {
            return WebClient
                    .builder()
                    .exchangeStrategies(jsonExchangeStrategies())
                    .baseUrl(baseURL)
                    .build();
        }
    }

    @ConditionalOnProperty(value = PIPELINE_REQUEST_MODE, havingValue = KAFKA)
    @Bean("kafkaMessageService")
    public MessageService httpMessageService(final KafkaTemplate<String, TriggerPipelineEvent> triggerPipelineEventKT,
                                             @Value("${kafka.pipeline-launch.topic}") final String launchPipelineTopicName) {
        return new KafkaMessageService(triggerPipelineEventKT, launchPipelineTopicName);
    }

    /**
     * Creates a {@link PipelineManagerService} bean.
     * This bean is used to manage the pipeline operations and requires instances of {@link MessageService}.
     * and {@link GlobusManagerService}.
     *
     * @param messageService the {@link MessageService} instance for handling messaging operations.
     * @param globusManagerService the {@link GlobusManagerService} instance for managing Globus operations.
     *
     * @return a {@link PipelineManagerService} instance configured with the provided services.
     */
    @Bean
    public PipelineManagerService pipelineManagerService(final MessageService messageService,
                                                         final GlobusManagerService globusManagerService) {
        return new PipelineManagerService(
                messageService,
                globusManagerService);
    }

    /**
     * Creates a {@link UserManagerService} bean.
     * This bean is used for managing user accounts and requires a {@link WebClient} instance for web requests,
     * basic authentication credentials, and a URI for user account management.
     *
     * @param userManagerWebClient the {@link WebClient} instance used for making web requests to the user manager.
     * @param basicAuth the basic authentication credentials for accessing the user manager.
     * @param userAccountURI the URI for user account management.
     *
     * @return a {@link UserManagerService} instance configured with the provided parameters.
     */
    @Bean
    public UserManagerService userManagerService(@Qualifier("userManagerWebClient") final WebClient userManagerWebClient,
                                                 @Value("${intervene.user-manager.user-account.uri}") final URI userAccountURI,
                                                 @Value("${user-manager.basic.auth}") final String basicAuth) {
        return new UserManagerService(
                userManagerWebClient,
                userAccountURI,
                basicAuth
        );
    }

    /**
     * Creates a {@link KeyHandlerService} bean.
     * This bean is used for handling key-related operations and requires a {@link WebClient} instance for web requests
     * and a URI for key handling services.
     *
     * @param keyHandlerServiceWebClient the {@link WebClient} instance used for making web requests to the key handler.
     * @param keyHandlerURI the URI for key handling services.
     *
     * @return a {@link KeyHandlerService} instance configured with the provided parameters.
     */
    @Bean
    public KeyHandlerService keyHandlerService(@Qualifier("keyHandlerWebClient") final WebClient keyHandlerServiceWebClient,
                                               @Value("${intervene.key-handler.keys.uri}") final URI keyHandlerURI) {
        return new KeyHandlerService(
                keyHandlerServiceWebClient,
                keyHandlerURI);
    }

    /**
     * Creates a {@link GlobusFileHandlerService} bean.
     * This bean is used for handling Globus file operations and requires a {@link WebClient} instance for web requests
     * and URIs for various Globus file handling operations.
     *
     * @param fileHandlerWebClient the {@link WebClient} instance used for making web requests to the Globus file handler.
     * @param globusUserURI the URI for retrieving Globus user details.
     * @param globusDirListFilesURI the URI for listing files in a Globus directory.
     * @param globusCreatDirURI the URI for creating directories in Globus.
     *
     * @return a {@link GlobusFileHandlerService} instance configured with the provided parameters.
     */
    @Bean
    public GlobusFileHandlerService fileHandlerService(@Qualifier("fileHandlerWebClient") final WebClient fileHandlerWebClient,
                                                       @Value("${intervene.file-handler.globus.user-details.uri}") final URI globusUserURI,
                                                       @Value("${intervene.file-handler.globus.list-files-dir.uri}") final URI globusDirListFilesURI,
                                                       @Value("${intervene.file-handler.globus.create-dir.uri}") final URI globusCreatDirURI) {
        return new GlobusFileHandlerService(
                fileHandlerWebClient,
                globusUserURI,
                globusDirListFilesURI,
                globusCreatDirURI
        );
    }

    /**
     * Creates a {@link GlobusManagerService} bean.
     * This bean is used for managing Globus operations and requires instances of {@link GlobusFileHandlerService},
     * {@link GlobusDetailsRepository}, and {@link GlobusUserRepository}.
     *
     * @param globusFileHandlerService the {@link GlobusFileHandlerService} instance for handling file operations.
     * @param globusDetailsRepository the {@link GlobusDetailsRepository} instance for managing Globus details.
     * @param globusUserRepository the {@link GlobusUserRepository} instance for managing Globus user data.
     *
     * @return a {@link GlobusManagerService} instance configured with the provided services and repositories.
     */
    @Bean
    public GlobusManagerService globusManagerService(final GlobusFileHandlerService globusFileHandlerService,
                                                     final GlobusDetailsRepository globusDetailsRepository,
                                                     final GlobusUserRepository globusUserRepository) {
        return new GlobusManagerService(
                globusFileHandlerService,
                globusDetailsRepository,
                globusUserRepository);
    }

    /**
     * Creates a {@link WebClient} bean for handling file operations.
     * This {@link WebClient} is configured with the base URL for the file handler service, which is used to perform
     * web requests related to file operations.
     *
     * @param fileHandlerBaseURL the base URL for the file handler service.
     *
     * @return a {@link WebClient} instance configured with the provided base URL.
     */
    @Bean("fileHandlerWebClient")
    public WebClient fileHandlerWebClient(@Value("${intervene.file-handler.base-url}") final String fileHandlerBaseURL) {
        return webClient(fileHandlerBaseURL);
    }

    /**
     * Creates a {@link WebClient} bean for managing user accounts.
     * This {@link WebClient} is configured with the base URL for the user manager service, which is used to perform
     * web requests related to user management.
     *
     * @param userManagerBaseURL the base URL for the user manager service.
     *
     * @return a {@link WebClient} instance configured with the provided base URL.
     */
    @Bean("userManagerWebClient")
    public WebClient userManagerWebClient(@Value("${intervene.user-manager.base-url}") final String userManagerBaseURL) {
        return webClient(userManagerBaseURL);
    }

    /**
     * Creates a {@link WebClient} bean for handling key management operations.
     * This {@link WebClient} is configured with the base URL for the key handler service, which is used to perform
     * web requests related to key management.
     *
     * @param keyHandlerBaseURL the base URL for the key handler service.
     *
     * @return a {@link WebClient} instance configured with the provided base URL.
     */
    @Bean("keyHandlerWebClient")
    public WebClient keyHandlerWebClient(@Value("${intervene.key-handler.base-url}") final String keyHandlerBaseURL) {
        return webClient(keyHandlerBaseURL);
    }

    private WebClient webClient(final String baseURL) {
        return WebClient
                .builder()
                .baseUrl(baseURL)
                .filter(new ServerBearerExchangeFilterFunction())
                .filter(propagateRequestId(LOGGER))
                .build();
    }

    private AmazonS3 amazonS3(final String endpoint,
                              final String accessKey,
                              final String secretKey,
                              final String region) {
        return AmazonS3ClientBuilder
                .standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .withPathStyleAccessEnabled(true)
                .build();
    }

    /**
     * Creates a {@link FileValidations} bean for validating files based on the {@link IGlobusFileDetailsWrapper} interface.
     * This bean provides file validation services using a generic wrapper type for file details.
     *
     * @return a {@link FileValidations} instance configured with default settings.
     */
    @Bean
    public FileValidations<IGlobusFileDetailsWrapper> fileValidations() {
        return new FileValidations<>();
    }

    /**
     * Customizes the {@link Jackson2ObjectMapperBuilder} with a specified date format for JSON serialization and deserialization.
     * This bean configures the Jackson {@link ObjectMapper} to use a custom date format for JSON processing, as specified
     * in the application properties.
     *
     * @param dateFormat the date format to be used by the {@link ObjectMapper}, injected from configuration properties.
     *
     * @return a {@link Jackson2ObjectMapperBuilderCustomizer} instance configured with the provided date format.
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer(@Value("${jackson.date-format}") final String dateFormat) {
        return CommonUtil.jackson2ObjectMapperBuilderCustomizer(dateFormat);
    }

    /**
     * Creates a {@link WebClient} bean for interacting with the PGS Catalog service, configured with a base URL.
     * This {@link WebClient} is set up to use the specified base URL for making REST API requests to the PGS Catalog service.
     *
     * @param baseURL the base URL for the PGS Catalog service, injected from configuration properties.
     *
     * @return a {@link WebClient} instance configured with the specified base URL.
     */
    @Bean("pgsCatalogWebClient")
    public WebClient webClientPublicURL(@Value("${pgs-catalog.rest.base-url}") final String baseURL) {
        return WebClient
                .builder()
                .baseUrl(baseURL)
                .build();
    }

    /**
     * Creates a {@link PGSCatalogService} bean for interacting with the PGS Catalog API.
     * This service is configured with a {@link WebClient} for making REST API requests and a URI for searching PGS traits.
     *
     * @param webClient the {@link WebClient} instance used to make API requests to the PGS Catalog service.
     * @param pgsTraitSearchURI the URI for searching PGS traits, injected from configuration properties.
     *
     * @return a {@link PGSCatalogService} instance configured with the provided {@link WebClient} and search URI.
     */
    @Bean
    public PGSCatalogService pgsCatalogService(@Qualifier("pgsCatalogWebClient") final WebClient webClient,
                                               @Value("${pgs-catalog.rest.search-url}") final URI pgsTraitSearchURI) {
        return new PGSCatalogService(
                webClient,
                pgsTraitSearchURI);
    }

    /**
     * Creates an {@link IEmailSender} bean for sending emails.
     * This bean uses {@link JavaMailSender} to send emails and is configured with the email address to be used as the sender.
     *
     * @param mailSender the {@link JavaMailSender} instance used for sending emails.
     * @param emailFrom the email address to be used as the sender, injected from configuration properties.
     *
     * @return an {@link IEmailSender} instance configured with the provided {@link JavaMailSender} and sender email address.
     */
    @Bean
    public IEmailSender emailService(final JavaMailSender mailSender,
                                     final @Value("${spring.mail.username}") String emailFrom) {
        return new EmailSender(mailSender, emailFrom);
    }

    /**
     * Creates {@link DPAConsentCheck} bean for checking user consent status on DPA.
     *
     * @param userManagerService the {@link UserManagerService} default implementation.
     *
     * @return {@link DPAConsentCheck} instance.
     */
    @Bean
    public DPAConsentCheck dpaConsentCheck(final IUserManagerService userManagerService) {
        return new DPAConsentCheck(userManagerService);
    }
}
