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

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.pipeline.manager.dto.DatasetDetailsDTO;
import uk.ac.ebi.gdp.intervene.pipeline.manager.mapper.DatasetMapper;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.DatasetDetails;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.DatasetDetailsRepository;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;
import static org.springframework.web.reactive.function.server.ServerResponse.status;
import static reactor.core.publisher.Mono.defer;
import static reactor.core.publisher.Mono.error;
import static uk.ac.ebi.gdp.intervene.commons.exception.ClientException.badRequest;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.FilesetType.GLOBUS;

public class DatasetRequestHandler {
    private final DatasetDetailsRepository datasetDetailsRepository;
    private final DatasetMapper datasetMapper;

    public DatasetRequestHandler(final DatasetDetailsRepository datasetDetailsRepository,
                                 final DatasetMapper datasetMapper) {
        this.datasetDetailsRepository = datasetDetailsRepository;
        this.datasetMapper = datasetMapper;
    }

    public Mono<ServerResponse> createOrUpdateDatasetDetails(final ServerRequest serverRequest) {
        return serverRequest
                .bodyToMono(DatasetDetailsDTO.class)
                .flatMap(datasetDetailsDTO ->
                        datasetDetailsRepository
                                .findByDatasetName(datasetDetailsDTO.getDatasetName())
                                .flatMap(datasetDetails -> {
                                    datasetDetails.updateGenomeBuild(datasetDetailsDTO.getGenomeBuild());
                                    return datasetDetailsRepository
                                            .save(datasetDetails)
                                            .flatMap(persistedDatasetDetails -> status(OK)
                                                    .bodyValue(persistedDatasetDetails.getDatasetId()));
                                })
                                .switchIfEmpty(
                                        defer(() -> buildDataset(datasetDetailsDTO))
                                                .cast(DatasetDetails.class)
                                                .flatMap(datasetDetailsRepository::save)
                                                .flatMap(datasetDetails -> status(CREATED)
                                                        .bodyValue(datasetDetails.getDatasetId()))));//TODO: check what details to return
    }

    private Mono<DatasetDetails> buildDataset(final DatasetDetailsDTO datasetDetailsDTO) {
        return datasetDetailsRepository
                .getNextDatasetId()
                .map(nextDatasetId -> datasetMapper.toModel(datasetDetailsDTO,
                        nextDatasetId,
                        GLOBUS));
    }

    public Mono<ServerResponse> getDatasetDetails(final ServerRequest serverRequest) {
        return serverRequest
                .queryParam("datasetId")
                .map(datasetId -> datasetDetailsRepository
                        .findById(datasetId)
                        .flatMap(datasetDetails -> ok().bodyValue(datasetMapper.toDTO(datasetDetails))))
                .orElse(error(badRequest("Query param 'datasetId' is not provided or empty!")));
    }
}
