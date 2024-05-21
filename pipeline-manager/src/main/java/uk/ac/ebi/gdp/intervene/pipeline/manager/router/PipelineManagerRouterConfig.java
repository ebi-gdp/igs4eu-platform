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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import uk.ac.ebi.gdp.intervene.commons.dto.filehandler.IGlobusFileDetailsWrapper;
import uk.ac.ebi.gdp.intervene.pipeline.manager.mapper.DatasetMapper;
import uk.ac.ebi.gdp.intervene.pipeline.manager.mapper.GlobusUserDetailsMapper;
import uk.ac.ebi.gdp.intervene.pipeline.manager.mapper.PipelineDetailsMapper;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.DatasetDetailsRepository;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.service.IPipelinePersistence;
import uk.ac.ebi.gdp.intervene.pipeline.manager.router.validation.FileValidations;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.GlobusManagerService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.ICloudStorage;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.PGSCatalogService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.PipelineManagerService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.UserManagerService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.utility.IEmailSender;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static uk.ac.ebi.gdp.intervene.commons.log.LogUtil.buildUniqueRequestId;

@Configuration
public class PipelineManagerRouterConfig {
    private final Logger LOGGER = LoggerFactory.getLogger(PipelineManagerRouterConfig.class);

    @Bean
    public RouterFunction<ServerResponse> pipelineRoutes(final PipelineRequestHandler pipelineRequestHandler,
                                                         final GlobusRequestHandler globusRequestHandler,
                                                         final CSCPipelineHandler cscPipelineHandler,
                                                         final PipelineResultHandler pipelineResultHandler,
                                                         final DatasetRequestHandler datasetRequestHandler) {
        return route()
                .filter(buildUniqueRequestId(LOGGER))
                .path("/pipeline", pb -> pb
                        .POST("/globus/dir-guest-collection", globusRequestHandler::createDirectoryOnGuestCollection)
                        .GET("/recent/top", serverRequest -> pipelineRequestHandler.getPipelineRecent())//TODO: rename path
                        .GET("/success/result", pipelineResultHandler::listResultFiles)
                        .POST("/pgs-ids/validate", pipelineRequestHandler::validatePGSIds)
                        .GET("/pgs-ids-catalog-traits", pipelineRequestHandler::searchPGSIdsByTraits)
                        .GET("/publication-data", pipelineRequestHandler::searchPGPIdsByPublications)
                        .path("/{pipelineId}", pbPId -> pbPId
                                .POST("/execute/pgs-ids", pipelineRequestHandler::executePipelineForPgsIds)
                                .POST("/execute/trait-ids", pipelineRequestHandler::executePipelineForTraitIds)
                                .POST("/execute/publication-ids", pipelineRequestHandler::executePipelineForPublicationIds)
                                .PATCH("/dataset", pipelineRequestHandler::updateDatasetId)
                                .GET("/report", pipelineResultHandler::streamFileFromCloudStorage)
                                .GET(pipelineRequestHandler::getPipeline))
                        .POST(pipelineRequestHandler::createPipeline)
                        .GET(pipelineRequestHandler::getPipelines))
                //Make sure this path is secured under basic auth, allows pipeline executor to trigger API
                .path("/integration/pipeline/{pipelineId}/status", pbCSC -> pbCSC.PATCH(cscPipelineHandler::updatePipelineStatus))
                .path("/dataset", db -> db
                        .GET("/{datasetId}", datasetRequestHandler::getDatasetDetails)
                        .GET(datasetRequestHandler::getDatasets)
                        .POST(datasetRequestHandler::createOrUpdateDatasetDetails))
                .path("/globus", gb -> gb
                        .POST("/user", globusRequestHandler::mapGlobusUserId)
                        .GET("/files/validate", globusRequestHandler::validateFiles))
                .build();
    }

    @Bean
    public PipelineRequestHandler pipelineRequestHandler(final PipelineManagerService pipelineManagerService,
                                                         final IPipelinePersistence pipelinePersistence,
                                                         final UserManagerService userManagerService,
                                                         final PipelineDetailsMapper pipelineDetailsMapper,
                                                         final StringRedisTemplate redisTemplate,
                                                         final PGSCatalogService pgsCatalogService,
                                                         @Value("${redis.pgs-ids.key-prefix}") final String redisPgsIdsKeyPrefix,
                                                         @Value("${redis.pud-data.key-prefix}") final String redisPubDataKeyPrefix) {
        return new PipelineRequestHandler(
                pipelineManagerService,
                pipelinePersistence,
                userManagerService,
                pipelineDetailsMapper,
                redisTemplate,
                pgsCatalogService,
                redisPgsIdsKeyPrefix,
                redisPubDataKeyPrefix);
    }

    @Bean
    public PipelineResultHandler pipelineResultHandler(final IPipelinePersistence pipelinePersistence,
                                                       final UserManagerService userManagerService,
                                                       final ICloudStorage cloudStorage,
                                                       @Value("${cloud.storage.bucket-name-format}") final String bucketNameFormat,
                                                       @Value("${cloud.storage.bucket-prefix}") final String bucketFilePrefix) {
        return new PipelineResultHandler(
                pipelinePersistence,
                userManagerService,
                cloudStorage,
                bucketNameFormat,
                bucketFilePrefix);
    }

    @Bean
    public DatasetRequestHandler datasetRequestHandler(final DatasetDetailsRepository datasetDetailsRepository,
                                                       final DatasetMapper datasetMapper,
                                                       final UserManagerService userManagerService) {
        return new DatasetRequestHandler(
                datasetDetailsRepository,
                datasetMapper,
                userManagerService);
    }

    @Bean
    public GlobusRequestHandler globusUserRequestHandler(final GlobusManagerService globusManagerService,
                                                         final UserManagerService userManagerService,
                                                         final GlobusUserDetailsMapper globusUserDetailsMapper,
                                                         final FileValidations<IGlobusFileDetailsWrapper> fileValidations) {
        return new GlobusRequestHandler(
                userManagerService,
                globusManagerService,
                globusUserDetailsMapper,
                fileValidations);
    }

    @Bean
    public CSCPipelineHandler cscPipelineHandler(final UserManagerService userManagerService,
                                                 final IPipelinePersistence pipelinePersistence,
                                                 final IEmailSender emailService,
                                                 @Value("${intervene.platform.url}") final String platformURL) {
        return new CSCPipelineHandler(
                userManagerService,
                pipelinePersistence,
                emailService,
                platformURL);
    }
}
