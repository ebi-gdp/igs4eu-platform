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

import com.amazonaws.services.s3.AmazonS3;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import uk.ac.ebi.gdp.intervene.pipeline.manager.mapper.DatasetMapper;
import uk.ac.ebi.gdp.intervene.pipeline.manager.mapper.GlobusUserDetailsMapper;
import uk.ac.ebi.gdp.intervene.pipeline.manager.mapper.PipelineDetailsMapper;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.DatasetDetailsRepository;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.GlobusUserRepository;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.service.IPipelinePersistence;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.GlobusFileHandlerService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.PipelineManagerService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.UserManagerService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.utility.IEmailSender;

import java.nio.file.Path;

import static java.nio.file.Paths.get;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class PipelineManagerRouterConfig {

    @Bean
    public RouterFunction<ServerResponse> pipelineRoutes(final PipelineRequestHandler pipelineRequestHandler,
                                                         final GlobusRequestHandler globusRequestHandler,
                                                         final CSCPipelineHandler cscPipelineHandler,
                                                         final PipelineResultHandler pipelineResultHandler) {
        final Path pipelineRoutes = get("/pipeline");
        return route() //TODO: revisit the logic. Look for nested routes
                .POST(pipelineRoutes.toString(), serverRequest -> pipelineRequestHandler.createPipeline())
                .GET(pipelineRoutes.resolve("{pipelineId}").toString(), pipelineRequestHandler::getPipelineDetails)
                .POST(pipelineRoutes.resolve("{pipelineId}/execute").toString(), pipelineRequestHandler::executePipeline)
                .POST(pipelineRoutes.resolve("{pipelineId}/globus/dir-guest-collection").toString(), globusRequestHandler::createDirectoryOnGuestCollection)
                .PATCH(pipelineRoutes.resolve("{pipelineId}/dataset").toString(), pipelineRequestHandler::updateDatasetId)
                .GET(pipelineRoutes.resolve("recent/top").toString(), serverRequest -> pipelineRequestHandler.getPipelineDetailsRecent())//TODO: rename path
                .POST(pipelineRoutes.resolve("csc/notify").toString(), cscPipelineHandler::pipelineNotificationCallback)
                .GET(pipelineRoutes.resolve("{pipelineId}/report").toString(), pipelineResultHandler::streamFileFromS3)
                .GET(pipelineRoutes.resolve("recent/result").toString(), serverRequest -> pipelineResultHandler.listResultFiles())
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> datasetRoutes(final DatasetRequestHandler datasetRequestHandler) {
        final Path datasetURI = get("/dataset");
        return route()
                .POST(datasetURI.toString(), datasetRequestHandler::createDatasetDetails)
                .GET(datasetURI.toString(), datasetRequestHandler::getDatasetDetails)
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> globusUserRoutes(final GlobusRequestHandler globusRequestHandler) {
        return route()
                .POST(get("/globus/user").toString(), globusRequestHandler::mapGlobusUserId)
                .build();
    }

    @Bean
    public PipelineRequestHandler pipelineRequestHandler(final PipelineManagerService pipelineManagerService,
                                                         final IPipelinePersistence pipelinePersistence,
                                                         final UserManagerService userManagerService,
                                                         final PipelineDetailsMapper pipelineDetailsMapper) {
        return new PipelineRequestHandler(
                pipelineManagerService,
                pipelinePersistence,
                userManagerService,
                pipelineDetailsMapper
        );
    }

    @Bean
    public PipelineResultHandler pipelineResultHandler(final IPipelinePersistence pipelinePersistence,
                                                       final UserManagerService userManagerService,
                                                       final AmazonS3 s3Client,
                                                       @Value("${ebi-embassy.s3.bucket-name}") final String s3Bucket) {
        return new PipelineResultHandler(
                pipelinePersistence,
                userManagerService,
                s3Client,
                s3Bucket
        );
    }

    @Bean
    public DatasetRequestHandler datasetRequestHandler(final DatasetDetailsRepository datasetDetailsRepository,
                                                       final DatasetMapper datasetMapper) {
        return new DatasetRequestHandler(
                datasetDetailsRepository,
                datasetMapper
        );
    }

    @Bean
    public GlobusRequestHandler globusUserRequestHandler(final PipelineManagerService pipelineManagerService,
                                                         final UserManagerService userManagerService,
                                                         final GlobusFileHandlerService globusFileHandlerService,
                                                         final GlobusUserRepository globusUserRepository,
                                                         final GlobusUserDetailsMapper globusUserDetailsMapper) {
        return new GlobusRequestHandler(
                pipelineManagerService,
                userManagerService,
                globusFileHandlerService,
                globusUserRepository,
                globusUserDetailsMapper
        );
    }

    @Bean
    public CSCPipelineHandler cscPipelineHandler(final UserManagerService userManagerService,
                                                 final IPipelinePersistence pipelinePersistence,
                                                 final IEmailSender emailService) {
        return new CSCPipelineHandler(
                userManagerService,
                pipelinePersistence,
                emailService
        );
    }
}
