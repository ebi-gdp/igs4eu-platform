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
package uk.ac.ebi.gdp.intervene.file.handler.service.globus.collection;

import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.commons.dto.filehandler.GlobusFileDetailsWrapperDTO;
import uk.ac.ebi.gdp.intervene.file.handler.service.globus.endpoint.PermissionType;

import java.nio.file.Path;

/**
 * Methods to provide access Globus APIs to perform various operations.
 * Add methods related to Globus operation.
 */
public interface IFileOperationService {
    /**
     * @param dirPath path of the dir to be created on Globus
     *
     * @return path
     */
    Mono<String> createDirectory(Path dirPath);

    /**
     * @param dirPath directory path for the files to search for
     *
     * @return GlobusFileDetailsWrapperDTO file details
     */
    Mono<GlobusFileDetailsWrapperDTO> listFiles(Path dirPath);

    /**
     * @param principal user UID
     * @param permissionType type of permission e.g. rw
     * @param notifyEmail user email id
     * @param dirPath permission to apply to
     *
     * @return UID
     */
    Mono<String> grantDirectoryPermission(String principal,
                                          PermissionType permissionType,
                                          String notifyEmail,
                                          Path dirPath);
}
