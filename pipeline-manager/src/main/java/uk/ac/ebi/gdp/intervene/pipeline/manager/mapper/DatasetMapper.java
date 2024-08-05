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
import uk.ac.ebi.gdp.intervene.pipeline.manager.dto.DatasetDetailsDTO;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.DatasetDetails;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.FilesetType;

import java.time.LocalDateTime;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;

@Mapper(componentModel = "spring", injectionStrategy = CONSTRUCTOR)
public interface DatasetMapper {
    @Mapping(target = "datasetId", source = "datasetId")
    @Mapping(target = "filesetType", source = "filesetType")
    @Mapping(target = "createdBy", source = "userId")
    @Mapping(target = "updatedBy", source = "userId")
    @Mapping(target = "expiresAt", source = "expiresAt")
    @Mapping(target = "genomeBuild", expression = "java(GenomeBuild.valueOf(datasetDetailsDTO.getGenomeBuild().toUpperCase()))")
    DatasetDetails toModel(DatasetDetailsDTO datasetDetailsDTO,
                           String datasetId,
                           FilesetType filesetType,
                           String userId,
                           LocalDateTime expiresAt);

    @Mapping(target = "globusDetails.username", source = "datasetDetails.globusDetails.globusUsername")
    @Mapping(target = "publicKey", source = "datasetDetails.datasetCryptographyDetails.publicKey")
    DatasetDetailsDTO toDTO(DatasetDetails datasetDetails);
}
