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

import java.nio.file.Path;

import static java.nio.file.Paths.get;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class UserRouterConfig {

    @Bean
    public RouterFunction<ServerResponse> userRoutes(final UserHandler userHandler) {
        final Path userAccount = get("/user/account");
        return route()
                .GET(userAccount.toString(), serverRequest -> userHandler.getCurrentUserAccount())
                .GET(userAccount.resolve("{accountId}").toString(), userHandler::getUserAccountDetails)
                .POST(userAccount.toString(), serverRequest -> userHandler.createUserAccount())
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
