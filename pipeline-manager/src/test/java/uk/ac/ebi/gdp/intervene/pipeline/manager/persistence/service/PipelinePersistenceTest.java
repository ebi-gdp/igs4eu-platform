/*
 *
 * Copyright 2024 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import uk.ac.ebi.gdp.intervene.pipeline.manager.dto.PipelineStatusDTO;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.common.TestConfig;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineDetails;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineExecutionStatus;

import java.time.LocalDateTime;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.common.TestData.DATASET_ID;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.common.TestData.PIPELINE_ID;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.common.TestData.USER_ID_ONE;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.common.TestUtility.DATE_FORMATTER;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.common.TestUtility.DATE_REGEX;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.common.TestUtility.UUID_PATTERN;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineStatus.COMPLETED;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineStatus.ERROR;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineStatus.NEW;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineStatus.PENDING;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineStatus.STARTED;

@DirtiesContext
@Import(TestConfig.class)
@EnableTransactionManagement
@EnableR2dbcAuditing
@ExtendWith(SpringExtension.class)
@DataR2dbcTest
public class PipelinePersistenceTest {

    @Autowired
    private IPipelinePersistence pipelinePersistence;

    @Test
    public void whenCreatePipelineWithValidData_thenCreatesPipeline() {
        StepVerifier
                .create(pipelinePersistence.createPipeline(USER_ID_ONE, DATASET_ID))
                .consumeNextWith(pipelineDetails -> {
                    assertThat(pipelineDetails.getPipelineId())
                            .isNotEmpty()
                            .isEqualTo(PIPELINE_ID);

                    assertThat(pipelineDetails.getPipelineUID())
                            .isNotEmpty()
                            .matches(UUID_PATTERN);

                    assertThat(pipelineDetails.getCreatedBy())
                            .isNotEmpty()
                            .isEqualTo(USER_ID_ONE);

                    assertThat(pipelineDetails.getCreatedOn()
                            .format(ofPattern(DATE_FORMATTER)))
                            .isNotNull()
                            .matches(DATE_REGEX);

                    assertThat(pipelineDetails.getUpdatedBy())
                            .isNotEmpty()
                            .isEqualTo(USER_ID_ONE);

                    assertThat(pipelineDetails.getUpdatedOn()
                            .format(ofPattern(DATE_FORMATTER)))
                            .isNotNull()
                            .matches(DATE_REGEX);

                    assertThat(pipelineDetails
                            .getPipelineExecutionStatus()
                            .getStatus())
                            .isEqualTo(NEW);

                    assertThat(pipelineDetails
                            .getDatasetId())
                            .isEqualTo(DATASET_ID);
                })
                .verifyComplete();
    }

    @Test
    public void whenCreateNewPipeline_thenDefaultStatusIsNew() {
        final Mono<PipelineDetails> pipelineDetailsMono = pipelinePersistence
                .createPipeline(USER_ID_ONE, DATASET_ID)
                .flatMap(pipelineExecutionStatus -> pipelinePersistence
                        .getPipeline(pipelineExecutionStatus.getPipelineId(), USER_ID_ONE));

        StepVerifier
                .create(pipelineDetailsMono)
                .assertNext(pipelineDetails -> assertEquals(NEW, pipelineDetails.getPipelineExecutionStatus().getStatus()))
                .verifyComplete();
    }

    @Test
    public void whenUpdatePipelineStatusWithStartedStatus_thenUpdatePipelineStatus() {
        final PipelineStatusDTO pipelineStatus = new PipelineStatusDTO.Builder(PIPELINE_ID, "Deployed", LocalDateTime.now())
                .build();

        final Mono<PipelineExecutionStatus> pipelineExecutionStatusMono = pipelinePersistence
                .createPipeline(USER_ID_ONE, DATASET_ID)
                .flatMap(pipelineDetails -> pipelinePersistence
                        .updatePipelineStatus(pipelineDetails.getPipelineId(), pipelineStatus));

        StepVerifier
                .create(pipelineExecutionStatusMono)
                .assertNext(pipelineExecutionStatus -> assertEquals(STARTED, pipelineExecutionStatus.getStatus()))
                .verifyComplete();
    }

    @Test
    public void whenUpdatePipelineStatusWithPendingStatus_thenUpdatePipelineStatus() {
        final PipelineStatusDTO pipelineStatus = new PipelineStatusDTO.Builder(PIPELINE_ID, "Pending", LocalDateTime.now())
                .build();

        final Mono<PipelineExecutionStatus> pipelineExecutionStatusMono = pipelinePersistence
                .createPipeline(USER_ID_ONE, DATASET_ID)
                .flatMap(pipelineDetails -> pipelinePersistence
                        .updatePipelineStatus(pipelineDetails.getPipelineId(), pipelineStatus));

        StepVerifier
                .create(pipelineExecutionStatusMono)
                .assertNext(pipelineExecutionStatus -> assertEquals(PENDING, pipelineExecutionStatus.getStatus()))
                .verifyComplete();
    }

    @Test
    public void whenUpdatePipelineStatusWithCompletedStatus_thenUpdatePipelineStatus() {
        final PipelineStatusDTO pipelineStatus = new PipelineStatusDTO.Builder(PIPELINE_ID, "Succeeded", LocalDateTime.now())
                .build();

        final Mono<PipelineDetails> pipelineDetailsMono = pipelinePersistence
                .createPipeline(USER_ID_ONE, DATASET_ID)
                .flatMap(pipelineDetails -> pipelinePersistence
                        .updatePipelineStatus(pipelineDetails.getPipelineId(), pipelineStatus))
                .flatMap(pipelineExecutionStatus -> pipelinePersistence
                        .getPipeline(pipelineExecutionStatus.getPipelineDetails().getPipelineId(), USER_ID_ONE));

        StepVerifier
                .create(pipelineDetailsMono)
                .assertNext(pipelineDetails -> assertEquals(COMPLETED, pipelineDetails.getPipelineExecutionStatus().getStatus()))
                .verifyComplete();
    }

    @Test
    public void whenUpdatePipelineStatusWithErrorStatus_thenUpdatePipelineStatus() {
        final PipelineStatusDTO pipelineStatus = new PipelineStatusDTO
                .Builder(PIPELINE_ID, "Failed", LocalDateTime.now())
                .traceExit((byte) 1)
                .traceName("dummy_trace_name")
                .build();

        final Mono<PipelineExecutionStatus> pipelineExecutionStatusMono = pipelinePersistence
                .createPipeline(USER_ID_ONE, DATASET_ID)
                .flatMap(pipelineDetails -> pipelinePersistence
                        .updatePipelineStatus(pipelineDetails.getPipelineId(), pipelineStatus));

        StepVerifier
                .create(pipelineExecutionStatusMono)
                .assertNext(pipelineExecutionStatus -> assertEquals(ERROR, pipelineExecutionStatus.getStatus()))
                .verifyComplete();
    }
}
