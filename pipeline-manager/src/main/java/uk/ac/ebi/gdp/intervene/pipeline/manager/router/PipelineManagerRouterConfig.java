/*
 *
 * Copyright 2023 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.gdp.intervene.pipeline.manager.router;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.thymeleaf.TemplateEngine;
import uk.ac.ebi.gdp.intervene.commons.dpa.DPAConsentCheck;
import uk.ac.ebi.gdp.intervene.commons.dto.filehandler.IGlobusFileDetailsWrapper;
import uk.ac.ebi.gdp.intervene.pipeline.manager.dto.validation.CreateDirDTOValidator;
import uk.ac.ebi.gdp.intervene.pipeline.manager.dto.validation.DatasetDetailsDTOValidator;
import uk.ac.ebi.gdp.intervene.pipeline.manager.dto.validation.ScoreIdsDTOValidator;
import uk.ac.ebi.gdp.intervene.pipeline.manager.mapper.DatasetMapper;
import uk.ac.ebi.gdp.intervene.pipeline.manager.mapper.GlobusUserDetailsMapper;
import uk.ac.ebi.gdp.intervene.pipeline.manager.mapper.PipelineDetailsMapper;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.DatasetCryptographyDetailsRepository;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.DatasetDetailsRepository;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.GlobusDetailsRepository;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.service.IPipelinePersistence;
import uk.ac.ebi.gdp.intervene.pipeline.manager.router.validation.FileValidations;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.CloudFileHandlerService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.GlobusFileHandlerService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.GlobusManagerService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.KeyHandlerService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.PGSCatalogService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.PipelineManagerService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.PipelineSubmissionRateLimiterFilter;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.UserManagerService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.utility.IEmailSender;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.config.OAuth2SecurityConfig.DATASET_EXPIRED_DIR;

/**
 * Defines router config beans.
 */
@Configuration
public class PipelineManagerRouterConfig {
    private final PipelineSubmissionRateLimiterFilter pipelineSubmissionRateLimiterFilter;

    public PipelineManagerRouterConfig(final PipelineSubmissionRateLimiterFilter pipelineSubmissionRateLimiterFilter) {
        this.pipelineSubmissionRateLimiterFilter = pipelineSubmissionRateLimiterFilter;
    }

    @Bean
    public RouterFunction<ServerResponse> pipelineRoutes(final PipelineRequestHandler pipelineRequestHandler,
                                                         final GlobusRequestHandler globusRequestHandler,
                                                         final PipelineResultHandler pipelineResultHandler,
                                                         final DatasetRequestHandler datasetRequestHandler,
                                                         final DPAConsentCheck dpaConsentCheck) {
        return route()
                .filter(dpaConsentCheck.hasUserGivenConsent())
                .path("/pipeline", pb -> pb
                        .GET("/success/result", pipelineResultHandler::listResultFiles)
                        .POST("/pgs-ids/validate", pipelineRequestHandler::validatePGSIds)
                        .GET("/pgs-ids-catalog-traits", pipelineRequestHandler::searchPGSIdsByTraits)
                        .GET("/publication-data", pipelineRequestHandler::searchPGPIdsByPublications)
                        .path("/{pipelineId}", pbPId -> pbPId
                                .path("/execute", pbExecute -> pbExecute
                                        .POST("/pgs-ids", pipelineRequestHandler::executePipelineForPgsIds)
                                        .POST("/trait-ids", pipelineRequestHandler::executePipelineForTraitIds)
                                        .POST("/publication-ids", pipelineRequestHandler::executePipelineForPublicationIds))
                                .PATCH("/dataset", pipelineRequestHandler::updateDatasetId)
                                .GET("/report", pipelineResultHandler::streamFileFromCloudStorage)
                                .GET(pipelineRequestHandler::getPipeline))
                        .POST(request ->
                                pipelineSubmissionRateLimiterFilter.isUserAllowedToSubmitPipeline(request, pipelineRequestHandler::createPipeline))
                        .GET(pipelineRequestHandler::getPipelines))
                .path("/dataset", db -> db
                        .GET("/{datasetId}", datasetRequestHandler::getDatasetDetails)
                        .DELETE("/{datasetId}", datasetRequestHandler::deleteDataset)
                        .GET(datasetRequestHandler::getDatasets)
                        .POST(datasetRequestHandler::createOrUpdateDatasetDetails))
                .path("/globus", gb -> gb
                        .POST("/user", globusRequestHandler::mapGlobusUserId)
                        .POST("/dir-guest-collection", globusRequestHandler::createDirectoryOnGuestCollection)
                        .GET("/files/validate", globusRequestHandler::validateFiles))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> pipelineRoutesBasicAuth(final PipelineStatusHandler pipelineStatusHandler) {
        return route()
                //Make sure this path is secured under basic auth, allows pipeline executor to trigger API
                .path("/integration/pipeline/{pipelineId}/status", pb -> pb.PATCH(pipelineStatusHandler::updatePipelineStatus))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> datasetRoutesBasicAuth(final DatasetRequestHandler datasetRequestHandler) {
        return route()
                //Make sure this path is secured under basic auth, allows pipeline executor to trigger API
                .path(DATASET_EXPIRED_DIR, db -> db.DELETE(serverRequest -> datasetRequestHandler.deleteGlobusDirsOnGuestCollectionInBatches()))
                .build();
    }

    @Bean
    public PipelineRequestHandler pipelineRequestHandler(final PipelineManagerService pipelineManagerService,
                                                         final IPipelinePersistence pipelinePersistence,
                                                         final PipelineDetailsMapper pipelineDetailsMapper,
                                                         final DatasetDetailsRepository datasetDetailsRepository,
                                                         final StringRedisTemplate redisTemplate,
                                                         final PGSCatalogService pgsCatalogService,
                                                         @Value("${redis.pgs-ids.key-prefix}") final String redisPgsIdsKeyPrefix,
                                                         @Value("${redis.pud-data.key-prefix}") final String redisPubDataKeyPrefix,
                                                         final ScoreIdsDTOValidator scoreIdsDTOValidator) {
        return new PipelineRequestHandler(
                pipelineManagerService,
                pipelinePersistence,
                pipelineDetailsMapper,
                datasetDetailsRepository,
                redisTemplate,
                pgsCatalogService,
                redisPgsIdsKeyPrefix,
                redisPubDataKeyPrefix,
                scoreIdsDTOValidator);
    }

    @Bean
    public PipelineResultHandler pipelineResultHandler(final IPipelinePersistence pipelinePersistence,
                                                       final CloudFileHandlerService cloudFileHandlerService,
                                                       @Value("${cloud.storage.bucket-name-format}") final String bucketNameFormat,
                                                       @Value("${cloud.storage.bucket-prefix}") final String bucketFilePrefix) {
        return new PipelineResultHandler(
                pipelinePersistence,
                cloudFileHandlerService,
                bucketNameFormat,
                bucketFilePrefix);
    }

    @Bean
    public DatasetRequestHandler datasetRequestHandler(final DatasetDetailsRepository datasetDetailsRepository,
                                                       final GlobusDetailsRepository globusDetailsRepository,
                                                       final DatasetCryptographyDetailsRepository datasetCryptographyDetailsRepository,
                                                       final DatasetMapper datasetMapper,
                                                       final KeyHandlerService keyHandlerService,
                                                       final GlobusFileHandlerService globusFileHandlerService,
                                                       final DatasetDetailsDTOValidator datasetDetailsDTOValidator) {
        return new DatasetRequestHandler(
                datasetDetailsRepository,
                globusDetailsRepository,
                datasetCryptographyDetailsRepository,
                datasetMapper,
                keyHandlerService,
                globusFileHandlerService,
                datasetDetailsDTOValidator);
    }

    @Bean
    public GlobusRequestHandler globusUserRequestHandler(final GlobusManagerService globusManagerService,
                                                         final GlobusUserDetailsMapper globusUserDetailsMapper,
                                                         final FileValidations<IGlobusFileDetailsWrapper> fileValidations,
                                                         final CreateDirDTOValidator createDirDTOValidator) {
        return new GlobusRequestHandler(
                globusManagerService,
                globusUserDetailsMapper,
                fileValidations,
                createDirDTOValidator);
    }

    @Bean
    public PipelineStatusHandler cscPipelineHandler(final UserManagerService userManagerService,
                                                    final IPipelinePersistence pipelinePersistence,
                                                    final IEmailSender emailService,
                                                    final TemplateEngine templateEngine,
                                                    @Value("${intervene.platform.url}") final String platformURL) {
        return new PipelineStatusHandler(
                userManagerService,
                pipelinePersistence,
                emailService,
                templateEngine,
                platformURL);
    }
}
