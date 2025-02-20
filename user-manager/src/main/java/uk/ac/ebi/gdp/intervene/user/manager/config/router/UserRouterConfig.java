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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import uk.ac.ebi.gdp.intervene.user.manager.auth.AuthenticationContext;
import uk.ac.ebi.gdp.intervene.user.manager.dpa.IAuditLogUserDPAConsentService;
import uk.ac.ebi.gdp.intervene.user.manager.handler.UserDPAConsentHandler;
import uk.ac.ebi.gdp.intervene.user.manager.handler.UserHandler;
import uk.ac.ebi.gdp.intervene.user.manager.mapper.UserAccountMapper;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.service.IUserAccountPersistenceService;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * User router config.
 *
 * @see RouterFunction
 * @see UserHandler
 */
@Configuration
public class UserRouterConfig {
    @Bean
    public RouterFunction<ServerResponse> userRoutes(final UserHandler userHandler,
                                                     final UserDPAConsentHandler userDPAConsentHandler) {
        return userRoutesConfig(userHandler, userDPAConsentHandler);
    }

    @Bean
    public RouterFunction<ServerResponse> userRoutesConfig(final UserHandler userHandler,
                                                           final UserDPAConsentHandler userDPAConsentHandler) {
        return route()
                .path("/user/account", ub -> ub
                        .path("/consent/data", cub -> cub
                                .POST(accept(APPLICATION_JSON), serverRequest -> userDPAConsentHandler.giveConsent())
                                .DELETE(serverRequest -> userDPAConsentHandler.revokeConsent())
                                .GET(serverRequest -> userDPAConsentHandler.getDPAConsentContent()))
                        .GET(path("/{accountId:^(?!consent$).+}"), userHandler::getUserAccount)
                        .GET(serverRequest -> userHandler.getUserAccount())
                        .POST(serverRequest -> userHandler.createUserAccount()))
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

    @Bean
    public UserDPAConsentHandler userConsentHandler(final IAuditLogUserDPAConsentService auditLogUserConsentService) {
        return new UserDPAConsentHandler(auditLogUserConsentService);
    }
}
