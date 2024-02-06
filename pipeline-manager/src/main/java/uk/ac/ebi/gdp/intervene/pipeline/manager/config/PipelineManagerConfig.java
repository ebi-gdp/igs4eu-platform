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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.oauth2.server.resource.web.reactive.function.client.ServerBearerExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
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
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.GlobusFileHandlerService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.GlobusManagerService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.PGSCatalogService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.PipelineManagerService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.UserManagerService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.message.AllasMessageService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.message.KafkaMessageService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.message.MessageService;

import java.net.URI;

import static uk.ac.ebi.gdp.intervene.commons.log.LogUtil.propagateRequestId;

/**
 * Bean config for pipeline manager service.
 */
@Import(ReactiveExceptionHandler.class)
@Configuration
public class PipelineManagerConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(PipelineManagerConfig.class);

    @Bean
    public IPipelinePersistence pipelinePersistence(final PipelineDetailsRepository pipelineDetailsRepository,
                                                    final PipelineExecutionStatusRepository pipelineExecutionStatusRepository,
                                                    final PipelineResultRepository pipelineResultRepository) {
        return new PipelinePersistence(
                pipelineDetailsRepository,
                pipelineExecutionStatusRepository,
                pipelineResultRepository);
    }

    @ConditionalOnProperty(value = "pipeline-execution.platform", havingValue = "EBI_EMBASSY")
    @Bean("kafkaMessageService")
    public MessageService kafkaMessageService(final KafkaTemplate<String, TriggerPipelineEvent> triggerPipelineEventKT,
                                              @Value("${kafka.pipeline-trigger.topic}") final String pipelineTriggerTopic) {
        return new KafkaMessageService(triggerPipelineEventKT, pipelineTriggerTopic);
    }

    @ConditionalOnProperty(value = "pipeline-execution.platform", havingValue = "CSC")
    @Bean("allasMessageService")
    public MessageService allasMessageService(@Qualifier("allasS3") final AmazonS3 s3ClientAllas,
                                              @Value("${s3.bucket-name}") final String bucketName) {
        return new AllasMessageService(s3ClientAllas, bucketName);
    }

    @Bean
    public PipelineManagerService pipelineManagerService(final MessageService messageService,
                                                         final GlobusManagerService globusManagerService) {
        return new PipelineManagerService(
                messageService,
                globusManagerService);
    }

    @Bean
    public UserManagerService userManagerService(@Qualifier("userManagerWebClient") final WebClient userManagerWebClient,
                                                 @Value("${user-manager.basic.auth}") final String basicAuth,
                                                 @Value("${intervene.user-manager.user-account.uri}") final URI userAccountURI) {
        return new UserManagerService(
                userManagerWebClient,
                basicAuth,
                userAccountURI
        );
    }

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

    @Bean
    public GlobusManagerService globusManagerService(final GlobusFileHandlerService globusFileHandlerService,
                                                     final GlobusDetailsRepository globusDetailsRepository,
                                                     final GlobusUserRepository globusUserRepository) {
        return new GlobusManagerService(
                globusFileHandlerService,
                globusDetailsRepository,
                globusUserRepository);
    }

    @Bean("fileHandlerWebClient")
    public WebClient fileHandlerWebClient(@Value("${intervene.file-handler.base-url}") final String fileHandlerBaseURL) {
        return webClient(fileHandlerBaseURL);
    }

    @Bean("userManagerWebClient")
    public WebClient userManagerWebClient(@Value("${intervene.user-manager.base-url}") final String userManagerBaseURL) {
        return webClient(userManagerBaseURL);
    }

    private WebClient webClient(final String baseURL) {
        return WebClient
                .builder()
                .baseUrl(baseURL)
                .filter(new ServerBearerExchangeFilterFunction())
                .filter(propagateRequestId(LOGGER))
                .build();
    }

    @ConditionalOnProperty(value = "pipeline-execution.platform", havingValue = "EBI_EMBASSY")
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

    @ConditionalOnProperty(value = "pipeline-execution.platform", havingValue = "CSC")
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

    @Bean
    public FileValidations<IGlobusFileDetailsWrapper> fileValidations() {
        return new FileValidations<>();
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer(@Value("${jackson.date-format}") final String dateFormat) {
        return CommonUtil.jackson2ObjectMapperBuilderCustomizer(dateFormat);
    }

    @Bean("pgsCatalogWebClient")
    public WebClient webClientPublicURL(@Value("${pgs-catalog.rest.base-url}") final String baseURL) {
        return WebClient
                .builder()
                .baseUrl(baseURL)
                .build();
    }

    @Bean
    public PGSCatalogService pgsCatalogService(@Qualifier("pgsCatalogWebClient") final WebClient webClient,
                                               @Value("${pgs-catalog.rest.search-url}") final URI pgsTraitSearchURI) {
        return new PGSCatalogService(
                webClient,
                pgsTraitSearchURI
        );
    }
}
