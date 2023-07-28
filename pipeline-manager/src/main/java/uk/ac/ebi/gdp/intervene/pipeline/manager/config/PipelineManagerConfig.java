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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.oauth2.server.resource.web.reactive.function.client.ServerBearerExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import uk.ac.ebi.gdp.intervene.commons.dto.filehandler.IGlobusFileDetailsWrapper;
import uk.ac.ebi.gdp.intervene.commons.exception.ReactiveExceptionHandler;
import uk.ac.ebi.gdp.intervene.pipeline.manager.message.TriggerPipelineEvent;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.GlobusDetailsRepository;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.GlobusUserRepository;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.PipelineDetailsRepository;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.PipelineResultRepository;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.service.IPipelinePersistence;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.service.PipelinePersistence;
import uk.ac.ebi.gdp.intervene.pipeline.manager.router.validation.FileValidations;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.GlobusFileHandlerService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.PipelineManagerService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.UserManagerService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.message.AllasMessageService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.message.KafkaMessageService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.message.MessageService;

import java.net.URI;

@Import(ReactiveExceptionHandler.class)
@Configuration
public class PipelineManagerConfig {

    @Bean
    public IPipelinePersistence pipelinePersistence(final PipelineDetailsRepository pipelineDetailsRepository,
                                                    final PipelineResultRepository pipelineResultRepository) {
        return new PipelinePersistence(
                pipelineDetailsRepository,
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
                                              @Value("${allas.s3.bucket-name}") final String bucketName) {
        return new AllasMessageService(s3ClientAllas, bucketName);
    }

    @Bean
    public PipelineManagerService pipelineManagerService(final MessageService messageService,
                                                         final GlobusFileHandlerService globusFileHandlerService,
                                                         final GlobusDetailsRepository globusDetailsRepository,
                                                         final GlobusUserRepository globusUserRepository) {
        return new PipelineManagerService(
                messageService,
                globusFileHandlerService,
                globusDetailsRepository,
                globusUserRepository
        );
    }

    @Bean
    public UserManagerService userManagerService(@Qualifier("userManagerWebClient") final WebClient userManagerWebClient,
                                                 @Value("${user-manager.basic.auth}") final String basicAuth) {
        return new UserManagerService(userManagerWebClient, basicAuth);
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

    @Bean("fileHandlerWebClient")
    public WebClient fileHandlerWebClient(@Value("${intervene.file-handler.base-url}") final String fileHandlerBaseURL) {
        return webClient(fileHandlerBaseURL);
    }

    @Bean("userManagerWebClient")
    public WebClient userManagerWebClient(@Value("${intervene.user-manager.base-url}") final String userManagerBaseURL) {
        return webClient(userManagerBaseURL);
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

    @Bean
    public FileValidations<IGlobusFileDetailsWrapper> fileValidations() {
        return new FileValidations<>();
    }

    private WebClient webClient(final String baseURL) {
        return WebClient
                .builder()
                .baseUrl(baseURL)
                .filter(new ServerBearerExchangeFilterFunction())
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
}
