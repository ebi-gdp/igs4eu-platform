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
package uk.ac.ebi.gdp.intervene.pipeline.manager.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.ac.ebi.gdp.intervene.pipeline.manager.dto.PipelineDetailsDTO;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineDetails;

@Mapper(componentModel = "spring")
public interface PipelineDetailsMapper {
    @Mapping(target = "pipelineStatus", source = "pipelineExecutionStatus.status")
    @Mapping(target = "traceName", source = "pipelineExecutionStatus.traceName")
    @Mapping(target = "traceExit", source = "pipelineExecutionStatus.traceExit")
    @Mapping(target = "submittedOn", source = "pipelineExecutionStatus.submittedOn")
    @Mapping(target = "startedOn", source = "pipelineExecutionStatus.startedOn")
    @Mapping(target = "endedOn", source = "pipelineExecutionStatus.endedOn")
    @Mapping(target = "globusDetails.dirPathOnGuestCollection", source = "datasetDetails.globusDetails.dirPathOnGuestCollection")
    @Mapping(target = "globusDetails.username", source = "datasetDetails.globusDetails.globusUsername")
    @Mapping(target = "globusDetails.userAccountUID", source = "datasetDetails.globusDetails.globusUserDetails.userUID")
    @Mapping(target = "globusDetails.guestCollectionId", source = "datasetDetails.globusDetails.guestCollectionId")
    @Mapping(target = "globusDetails.filesetId", source = "datasetDetails.globusDetails.filesetId")
    PipelineDetailsDTO toDTO(PipelineDetails pipelineDetails);
}
