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
package uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.DatasetDetails;

import java.time.LocalDateTime;

@Repository
public interface DatasetDetailsRepository extends R2dbcRepository<DatasetDetails, String>,
        ReactiveSortingRepository<DatasetDetails, String>,
        CustomDatasetDetailsRepository {
    @Query(value = "CALL GET_NEXT_DATASET_ID('');")
    Mono<String> getNextDatasetId();

    Flux<DatasetDetails> findAllByCreatedBy(String createdBy, Pageable pageable);

    Mono<Long> countAllByCreatedBy(String userId);

    Mono<DatasetDetails> findByDatasetIdAndCreatedByAndExpiresAtAfter(String datasetId, String createdBy, LocalDateTime expiresAt);

    /**
     * Default method to retrieve active dataset,
     * calls {@link #findByDatasetIdAndCreatedByAndExpiresAtAfter(String, String, LocalDateTime)} method internally
     *
     * @param datasetId dataset id
     * @param createdBy user id
     *
     * @return dataset details
     */
    default Mono<DatasetDetails> findActiveDataset(final String datasetId,
                                                   final String createdBy) {
        return findByDatasetIdAndCreatedByAndExpiresAtAfter(datasetId, createdBy, LocalDateTime.now());
    }
}

