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
package uk.ac.ebi.gdp.intervene.user.manager.config.router;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import uk.ac.ebi.gdp.intervene.commons.dto.usermanager.UserAccountDTO;
import uk.ac.ebi.gdp.intervene.user.manager.auth.AuthenticationContext;
import uk.ac.ebi.gdp.intervene.user.manager.handler.UserHandler;
import uk.ac.ebi.gdp.intervene.user.manager.mapper.UserAccountMapper;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.service.IUserAccountPersistenceService;

import java.nio.file.Path;

import static java.nio.file.Paths.get;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static uk.ac.ebi.gdp.intervene.commons.log.LogUtil.logRequestIdHeader;

/**
 * User router config.
 *
 * @see RouterFunction
 * @see UserHandler
 */
@Configuration
public class UserRouterConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserRouterConfig.class);

    @RouterOperations({
            @RouterOperation(path = "/user/account", produces = {
                    MediaType.APPLICATION_JSON_VALUE},
                    operation = @Operation(operationId = "getUser", responses = {
                            @ApiResponse(responseCode = "200", description = "successful operation",
                                    content = @Content(schema = @Schema(implementation = UserAccountDTO.class)))}
                    ))
    })
    @Bean
    public RouterFunction<ServerResponse> userRoutes(final UserHandler userHandler) {
        final Path userAccount = get("/user/account");
        return route()
                .filter(logRequestIdHeader(LOGGER))
                .GET(userAccount.toString(), serverRequest -> userHandler.getUserAccount())
                .GET(userAccount.resolve("{accountId}").toString(), userHandler::getUserAccount)
                .POST(userAccount.toString(), serverRequest -> userHandler.createUserAccount())
                .build();
    }

    @Bean
    public UserHandler userHandler(final IUserAccountPersistenceService userAccountPersistenceService,
                                   final UserAccountMapper userAccountMapper,
                                   final AuthenticationContext authenticationContext) {
        return new UserHandler(
                userAccountPersistenceService,
                userAccountMapper,
                authenticationContext);
    }
}
