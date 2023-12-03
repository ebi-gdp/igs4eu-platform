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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.commons.dto.usermanager.UserAccountDTO;
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

/**
 * User request handler. Defines handlers for router functions.
 */
public class UserHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserHandler.class);
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

    /**
     * Get current user account based on access token.
     *
     * @return user account details represented by {@link UserAccountDTO}
     */
    public Mono<ServerResponse> getCurrentUserAccount() {
        return currentUserId()
                .flatMap(userAccountId -> userAccountPersistenceService
                        .getUserAccountByAuthUserAccountId(userAccountId)
                        .doOnNext(authUserAccountR2DBC -> LOGGER.info("User Id: {}", authUserAccountR2DBC.getUserAccount().getUserId()))
                        .flatMap(authUserAccountR2DBC -> ok()
                                .bodyValue(userAccountMapper.toDTO(authUserAccountR2DBC.getUserAccount())))
                        .switchIfEmpty(error(resourceNotFound(format("User account having auth id %s not found",
                                userAccountId)))));
    }

    /**
     * Get user account details for requested account id.
     *
     * @param serverRequest represents a server-side HTTP request, as handled by a {@code HandlerFunction}.
     *
     * @return user account details represented by {@link UserAccountDTO}
     */
    public Mono<ServerResponse> getUserAccountDetails(final ServerRequest serverRequest) {
        return userAccountPersistenceService
                .getUserAccountById(serverRequest.pathVariable("accountId"))
                .flatMap(userAccount -> ok()
                        .bodyValue(userAccountMapper.toDTO(userAccount)));
    }

    /**
     * Creates new user account.
     *
     * @return newly created user account details represented by {@link UserAccountDTO}
     */
    public Mono<ServerResponse> createUserAccount() {
        return currentUserId()
                .flatMap(userManagerService::createUserAccount)
                .flatMap(userAccountR2DBC -> status(CREATED)
                        .bodyValue(userAccountMapper.toDTO((userAccountR2DBC))));
    }
}
