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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.pipeline.manager.constant.GenomeBuild;
import uk.ac.ebi.gdp.intervene.pipeline.manager.dto.DatasetDetailsDTO;
import uk.ac.ebi.gdp.intervene.pipeline.manager.dto.PaginationDTO;
import uk.ac.ebi.gdp.intervene.pipeline.manager.mapper.DatasetMapper;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.DatasetDetails;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.DatasetDetailsRepository;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.UserManagerService;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;
import static org.springframework.web.reactive.function.server.ServerResponse.status;
import static reactor.core.publisher.Mono.defer;
import static reactor.core.publisher.Mono.error;
import static uk.ac.ebi.gdp.intervene.commons.exception.ClientException.resourceNotFound;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.constant.GenomeBuild.valueOf;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.FilesetType.GLOBUS;

/**
 * Request handler for Dataset related operations.
 */
public class DatasetRequestHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetRequestHandler.class);
    private final DatasetDetailsRepository datasetDetailsRepository;
    private final DatasetMapper datasetMapper;
    private final UserManagerService userManagerService;

    public DatasetRequestHandler(final DatasetDetailsRepository datasetDetailsRepository,
                                 final DatasetMapper datasetMapper,
                                 final UserManagerService userManagerService) {
        this.datasetDetailsRepository = datasetDetailsRepository;
        this.datasetMapper = datasetMapper;
        this.userManagerService = userManagerService;
    }

    /**
     * Creates/Updates dataset details.
     *
     * @param serverRequest represents a server-side HTTP request, as handled by a {@code HandlerFunction}
     *
     * @return dataset id represented by {@link String}
     */
    @Transactional
    public Mono<ServerResponse> createOrUpdateDatasetDetails(final ServerRequest serverRequest) {
        return serverRequest
                .bodyToMono(DatasetDetailsDTO.class)
                .flatMap(datasetDetailsDTO -> userManagerService
                        .getUserAccountDetails()
                        .flatMap(userAccountDTO -> datasetDetailsRepository
                                .findByDatasetIdAndCreatedBy(datasetDetailsDTO.getFilesetId(), userAccountDTO.accountId())
                                .flatMap(datasetDetails -> updateDataset(datasetDetails,
                                        valueOf(datasetDetailsDTO.getGenomeBuild().toUpperCase())))
                                .switchIfEmpty(defer(() -> createDataset(datasetDetailsDTO, userAccountDTO.accountId())))));

    }

    private Mono<ServerResponse> updateDataset(final DatasetDetails datasetDetails,
                                               final GenomeBuild genomeBuild) {
        LOGGER.info("Dataset details are being updated");
        datasetDetails.updateGenomeBuild(genomeBuild);
        return datasetDetailsRepository
                .save(datasetDetails)
                .flatMap(persistedDatasetDetails -> status(OK)
                        .bodyValue(persistedDatasetDetails.getDatasetId()))
                .doOnNext(serverResponse -> LOGGER.info("Dataset details have been updated"));
    }

    private Mono<ServerResponse> createDataset(final DatasetDetailsDTO datasetDetailsDTO,
                                               final String accountId) {
        return buildDataset(datasetDetailsDTO, accountId)
                .doOnNext(datasetDetails -> LOGGER.info("Dataset details are being created"))
                .flatMap(datasetDetailsRepository::save)
                .flatMap(datasetDetails -> status(CREATED)
                        .bodyValue(datasetDetails.getDatasetId()))
                .doOnNext(serverResponse -> LOGGER.info("Dataset details have been created"));
    }

    private Mono<DatasetDetails> buildDataset(final DatasetDetailsDTO datasetDetailsDTO,
                                              final String userId) {
        return datasetDetailsRepository
                .getNextDatasetId()
                .map(nextDatasetId -> datasetMapper.toModel(datasetDetailsDTO,
                        nextDatasetId,
                        GLOBUS,
                        userId));
    }

    /**
     * Retrieves dataset details.
     *
     * @param serverRequest represents a server-side HTTP request, as handled by a {@code HandlerFunction}
     *
     * @return Dataset details represented by {@link DatasetDetailsDTO} Or http status 404(NotFound)
     * @see HttpStatus
     */
    public Mono<ServerResponse> getDatasetDetails(final ServerRequest serverRequest) {
        final String datasetId = serverRequest.pathVariable("datasetId");
        return userManagerService
                .getUserAccountDetails()
                .doOnNext(userAccountDTO -> LOGGER.info("Retrieving dataset details: {} for the user: {}", datasetId, userAccountDTO.accountId()))
                .flatMap(userAccountDTO -> datasetDetailsRepository
                        .findByDatasetIdAndCreatedBy(datasetId,
                                userAccountDTO.accountId())
                        .flatMap(datasetDetails -> {
                            LOGGER.info("Retrieved dataset details: {} for the user: {}", datasetId, userAccountDTO.accountId());
                            return ok().bodyValue(datasetMapper.toDTO(datasetDetails));
                        })
                        .switchIfEmpty(error(resourceNotFound("Dataset Id %s not found!".formatted(datasetId)))));
    }

    /**
     * Retrieves all datasets for user.
     *
     * @param serverRequest represents a server-side HTTP request, as handled by a {@code HandlerFunction}
     *
     * @return Paginated list of datasets represented by {@link PaginationDTO}
     */
    public Mono<ServerResponse> getDatasets(final ServerRequest serverRequest) {
        return userManagerService
                .getUserAccountDetails()
                .doOnNext(userAccountDTO -> LOGGER.info("Retrieving all datasets"))
                .flatMap(userAccountDTO -> datasetDetailsRepository
                        .countAllByCreatedBy(userAccountDTO.accountId())
                        .flatMap(count -> doGetDatasetDetails(serverRequest, userAccountDTO.accountId())
                                .map(pipelineDetailsDTOS -> new PaginationDTO<>(pipelineDetailsDTOS, count)))
                        .flatMap(paginationDTO -> ok().bodyValue(paginationDTO))
                        .doOnNext(serverResponse -> LOGGER.info("Retrieved all datasets")));
    }

    private Mono<List<DatasetDetailsDTO>> doGetDatasetDetails(final ServerRequest serverRequest,
                                                              final String accountId) {
        final int page = serverRequest.queryParam("page").map(Integer::parseInt).orElse(0);
        final int size = serverRequest.queryParam("size").map(Integer::parseInt).orElse(10);
        LOGGER.debug("Page number: {}, size: {}", page, size);
        return datasetDetailsRepository
                .findAllByCreatedBy(accountId, PageRequest.of(page, size, Sort.by("datasetId").descending()))
                .collectList()
                .map(this::buildDatasetList);
    }

    private List<DatasetDetailsDTO> buildDatasetList(final List<DatasetDetails> datasetDetailsMono) {
        return datasetDetailsMono
                .parallelStream()
                .map(datasetMapper::toDTO)
                .collect(toList());
    }
}
