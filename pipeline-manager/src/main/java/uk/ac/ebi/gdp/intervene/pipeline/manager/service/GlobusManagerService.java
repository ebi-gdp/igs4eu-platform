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
package uk.ac.ebi.gdp.intervene.pipeline.manager.service;

import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.commons.dto.filehandler.GlobusFileDetailsWrapperDTO;
import uk.ac.ebi.gdp.intervene.commons.dto.filehandler.GuestCollectionDirReqDTO;
import uk.ac.ebi.gdp.intervene.pipeline.manager.dto.GlobusDetailsDTO;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.GlobusDetails;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.GlobusUserDetails;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.GlobusDetailsRepository;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.GlobusUserRepository;

import java.nio.file.Path;

import static reactor.core.publisher.Mono.defer;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.GlobusUserDetails.create;

/**
 * Globus manager service, service to perform Globus operations.
 */
public class GlobusManagerService {
    private final GlobusFileHandlerService globusFileHandlerService;
    private final GlobusDetailsRepository globusDetailsRepository;
    private final GlobusUserRepository globusUserRepository;

    /**
     * Constructs a {@code GlobusManagerService} instance.
     * This service is responsible for managing operations related to Globus, including handling files,
     * accessing details, and managing users associated with Globus.
     *
     * @param globusFileHandlerService the service responsible for handling file operations in Globus.
     * @param globusDetailsRepository the repository for accessing and managing Globus details.
     * @param globusUserRepository the repository for managing Globus user information.
     */
    public GlobusManagerService(final GlobusFileHandlerService globusFileHandlerService,
                                final GlobusDetailsRepository globusDetailsRepository,
                                final GlobusUserRepository globusUserRepository) {
        this.globusFileHandlerService = globusFileHandlerService;
        this.globusDetailsRepository = globusDetailsRepository;
        this.globusUserRepository = globusUserRepository;
    }

    /**
     * Creates directory on guest collection.
     *
     * @param directoryName name of the directory
     * @param username globus username, user for which dir to be created
     *
     * @return Globus details reprsented by {@link GlobusDetailsDTO}
     */
    public Mono<GlobusDetailsDTO> createDirectoryOnGuestCollection(final Path directoryName,
                                                                   final String username) {
        return globusUserRepository
                .findById(username)
                .map(globusUserDetails -> new GuestCollectionDirReqDTO(
                        directoryName.toString(),
                        globusUserDetails.getUserUID(),
                        globusUserDetails.getUsername()))
                .flatMap(globusFileHandlerService::createDirectoryOnGuestCollection);
    }

    /**
     * Creates/Updates Globus record.
     *
     * @param globusUsername globus username
     * @param guestCollectionId guest collection id
     * @param dirPathOnGuestCollection dir path on guest collection
     *
     * @return Globus details represented by {@link GlobusDetails}
     */
    public Mono<GlobusDetails> createOrUpdateGlobusRecord(final String globusUsername,
                                                          final String guestCollectionId,
                                                          final Path dirPathOnGuestCollection) {
        return globusDetailsRepository
                .findByDirPathOnGuestCollectionEndsWith(dirPathOnGuestCollection.toString())
                .flatMap(globusDetails -> {
                    globusDetails.updateGlobusUsername(globusUsername);
                    return globusDetailsRepository.save(globusDetails);
                })
                .switchIfEmpty(defer(() -> globusDetailsRepository
                        .getNextFilesetId()
                        .map(nextFilesetId ->
                                GlobusDetails.create(
                                        nextFilesetId,
                                        globusUsername,
                                        guestCollectionId,
                                        dirPathOnGuestCollection
                                ))
                        .flatMap(globusDetailsRepository::save))
                );
    }

    /**
     * Lists files on guest collection.
     *
     * @param dirPath dir path on guest collection
     *
     * @return {@link GlobusFileDetailsWrapperDTO}
     */
    public Mono<GlobusFileDetailsWrapperDTO> listFilesOnGuestCollection(final Path dirPath) {
        return globusFileHandlerService
                .listFilesOnGuestCollection(dirPath);
    }

    /**
     * Get Globus user details.
     *
     * @param username globus username
     * @param accountId intervene account id
     *
     * @return {@link GlobusUserDetails}
     */
    public Mono<GlobusUserDetails> getUserDetails(final String username,
                                                  final String accountId) {
        return globusFileHandlerService
                .getGlobusUserDetails(username)
                .map(globusUserIDWDto -> create(
                        username,
                        globusUserIDWDto.getIdentities().get(0).getUid(),
                        accountId));
    }

    /**
     * Get Globus user details.
     *
     * @param username globus username
     *
     * @return {@link GlobusUserDetails}
     */
    public Mono<GlobusUserDetails> getUserDetails(final String username) {
        return globusUserRepository
                .findById(username);
    }

    /**
     * Persist {@link GlobusUserDetails}
     *
     * @param globusUserDetails object of type {@link GlobusUserDetails} to persist
     *
     * @return {@link GlobusUserDetails}
     */
    public Mono<GlobusUserDetails> save(final GlobusUserDetails globusUserDetails) {
        return globusUserRepository.save(globusUserDetails);
    }
}
