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
package uk.ac.ebi.gdp.intervene.user.manager.handler;

import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.user.manager.mapper.UserAccountMapper;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.service.IUserAccountPersistenceService;
import uk.ac.ebi.gdp.intervene.user.manager.service.IUserManagerService;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;
import static org.springframework.web.reactive.function.server.ServerResponse.status;
import static reactor.core.publisher.Mono.error;
import static uk.ac.ebi.gdp.intervene.commons.exception.ClientException.resourceNotFound;
import static uk.ac.ebi.gdp.intervene.commons.security.SecurityContextDataProvider.currentUserId;

public class UserHandler {
    private final IUserManagerService userManagerService;
    private final IUserAccountPersistenceService userAccountPersistenceService;
    private final UserAccountMapper userAccountMapper;

    public UserHandler(final IUserManagerService userManagerService,
                       final IUserAccountPersistenceService userAccountPersistenceService,
                       final UserAccountMapper userAccountMapper) {
        this.userManagerService = userManagerService;
        this.userAccountPersistenceService = userAccountPersistenceService;
        this.userAccountMapper = userAccountMapper;
    }

    public Mono<ServerResponse> getUserAccount() {
        return currentUserId()
                .flatMap(userAccountId -> userAccountPersistenceService
                        .getUserAccount(userAccountId)
                        .flatMap(authUserAccountR2DBC -> ok().bodyValue(userAccountMapper.toDTO(authUserAccountR2DBC.getUserAccount())))
                        .switchIfEmpty(error(resourceNotFound(format("User account having auth id %s not found",
                                userAccountId)))));
    }

    public Mono<ServerResponse> createUserAccount() {
        return currentUserId()
                .flatMap(userManagerService::createUserAccount)
                .flatMap(userAccountR2DBC -> status(CREATED).bodyValue(userAccountMapper.toDTO((userAccountR2DBC))));
    }
}
