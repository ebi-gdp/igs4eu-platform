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
package uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineDetails;

import java.time.LocalDateTime;

@Repository
public interface PipelineDetailsRepository extends R2dbcRepository<PipelineDetails, String>,
        CustomPipelineDetailsRepository {
    @Query("CALL GET_NEXT_INTERVENE_PIPELINE_ID('');")
    Mono<String> getNextPipelineId();

    Mono<Long> countAllByUserId(String userId);

    Flux<PipelineDetails> findAllByUserIdAndCreatedByAndCreatedOnBetween(String userId,
                                                                         String createdBy,
                                                                         LocalDateTime createdOnAfter,
                                                                         LocalDateTime createdOnBefore);
}

