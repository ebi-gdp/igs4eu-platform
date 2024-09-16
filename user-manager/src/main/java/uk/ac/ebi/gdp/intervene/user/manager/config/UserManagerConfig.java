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
import uk.ac.ebi.gdp.intervene.user.manager.dpa.AuditLogUserDPAConsentService;
import uk.ac.ebi.gdp.intervene.user.manager.dpa.IAuditLogUserDPAConsentService;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.repository.AuthUserAccountRepository;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.repository.UserAccountDetailsRepository;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.repository.UserAccountRepository;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.repository.UserConsentAuditLogRepository;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.repository.UserConsentDetailsRepository;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.service.IUserAccountPersistenceService;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.service.UserAccountPersistenceService;

/**
 * User manager config.
 *
 * @see ReactiveExceptionHandler
 * @see IUserAccountPersistenceService
 */
@Import(ReactiveExceptionHandler.class)
@Configuration
public class UserManagerConfig {

    @Bean
    public IUserAccountPersistenceService userAccountPersistenceService(final UserAccountRepository userAccountRepository,
                                                                        final UserAccountDetailsRepository userAccountDetailsRepository,
                                                                        final AuthUserAccountRepository authUserAccountRepository) {
        return new UserAccountPersistenceService(
                userAccountRepository,
                userAccountDetailsRepository,
                authUserAccountRepository
        );
    }

    /**
     * Implementation to handle user consent database status.
     *
     * @param userAccountPersistenceService {@link UserAccountPersistenceService} instance.
     * @param userConsentAuditLogRepository {@link UserConsentAuditLogRepository} instance.
     * @param userConsentDetailsRepository {@link UserConsentDetailsRepository} instance.
     *
     * @return {@link AuditLogUserDPAConsentService} default implementation instance.
     */
    @Bean
    public IAuditLogUserDPAConsentService userConsentService(final IUserAccountPersistenceService userAccountPersistenceService,
                                                             final UserConsentAuditLogRepository userConsentAuditLogRepository,
                                                             final UserConsentDetailsRepository userConsentDetailsRepository) {
        return new AuditLogUserDPAConsentService(
                userAccountPersistenceService,
                userConsentAuditLogRepository,
                userConsentDetailsRepository);
    }
}
