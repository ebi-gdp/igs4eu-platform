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
import uk.ac.ebi.gdp.intervene.user.manager.handler.UserHandler;
import uk.ac.ebi.gdp.intervene.user.manager.mapper.UserAccountMapper;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.service.IUserAccountPersistenceService;
import uk.ac.ebi.gdp.intervene.user.manager.service.IUserManagerService;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class UserRouterConfig {

    @Bean
    RouterFunction<ServerResponse> userRoutes(final UserHandler userHandler) {
        final String userAccountURI = "/user/account";
        return route()
                .GET(userAccountURI, accept(APPLICATION_JSON), serverRequest -> userHandler.getUserAccount())
                .POST(userAccountURI, accept(APPLICATION_JSON), serverRequest -> userHandler.createUserAccount())
                .build();
    }

    @Bean
    public UserHandler userHandler(final IUserManagerService userManagerService,
                                   final IUserAccountPersistenceService userAccountPersistenceService,
                                   final UserAccountMapper userAccountMapper) {
        return new UserHandler(
                userManagerService,
                userAccountPersistenceService,
                userAccountMapper
        );
    }
}
