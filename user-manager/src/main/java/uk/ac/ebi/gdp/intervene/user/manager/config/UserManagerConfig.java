/*
 *
 * Copyright 2022 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.gdp.intervene.user.manager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import uk.ac.ebi.gdp.intervene.commons.exception.ReactiveExceptionHandler;
import uk.ac.ebi.gdp.intervene.user.manager.auth.AuthenticationContext;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.repository.AuthUserAccountRepository;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.repository.UserAccountDetailsRepository;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.repository.UserAccountRepository;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.service.IUserAccountPersistenceService;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.service.UserAccountPersistenceService;
import uk.ac.ebi.gdp.intervene.user.manager.service.IUserManagerService;
import uk.ac.ebi.gdp.intervene.user.manager.service.UserManagerService;

@Import(ReactiveExceptionHandler.class)
@Configuration
public class UserManagerConfig {

    @Bean
    public IUserAccountPersistenceService userAccountPersistenceService(final UserAccountRepository userAccountRepository,
                                                                        final UserAccountDetailsRepository userAccountDetailsRepository,
                                                                        final AuthUserAccountRepository authUserAccountRepository,
                                                                        final AuthenticationContext authenticationContext) {
        return new UserAccountPersistenceService(
                userAccountRepository,
                userAccountDetailsRepository,
                authUserAccountRepository,
                authenticationContext
        );
    }

    @Bean
    public IUserManagerService userManagerService(final IUserAccountPersistenceService userAccountPersistenceService) {
        return new UserManagerService(userAccountPersistenceService);
    }
}
