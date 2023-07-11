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

public interface IFileOperationService {
    Mono<String> createDirectory(Path dirPath);

    Mono<GlobusFileDetailsWrapperDTO> listFiles(Path path);

    Mono<String> grantDirectoryPermission(String principal,
                                          PermissionType permissionType,
                                          String notifyEmail,
                                          Path path);
}
