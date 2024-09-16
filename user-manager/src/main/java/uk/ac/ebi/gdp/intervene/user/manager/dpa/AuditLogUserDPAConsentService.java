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
package uk.ac.ebi.gdp.intervene.user.manager.dpa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.AuthUserAccount;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.UserAccount;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.UserDPAConsentAuditLog;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.UserDPAConsentDetails;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.UserDPAConsentType;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.repository.UserConsentAuditLogRepository;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.repository.UserConsentDetailsRepository;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.service.IUserAccountPersistenceService;

import static java.lang.Boolean.FALSE;
import static reactor.core.publisher.Mono.just;
import static uk.ac.ebi.gdp.intervene.commons.security.SecurityContextDataProvider.currentUserId;
import static uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.UserDPAConsentType.GIVEN;
import static uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.UserDPAConsentType.REVOKED;

/**
 * Audit log user DPA consent service.
 */
public class AuditLogUserDPAConsentService implements IAuditLogUserDPAConsentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditLogUserDPAConsentService.class);
    private final IUserAccountPersistenceService userAccountPersistenceService;
    private final UserConsentAuditLogRepository userConsentAuditLogRepository;
    private final UserConsentDetailsRepository userConsentDetailsRepository;

    public AuditLogUserDPAConsentService(final IUserAccountPersistenceService userAccountPersistenceService,
                                         final UserConsentAuditLogRepository userConsentAuditLogRepository,
                                         final UserConsentDetailsRepository userConsentDetailsRepository) {
        this.userAccountPersistenceService = userAccountPersistenceService;
        this.userConsentAuditLogRepository = userConsentAuditLogRepository;
        this.userConsentDetailsRepository = userConsentDetailsRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<UserDPAConsent> fetchDPAContent() {
        return userConsentDetailsRepository
                .findFirstByOrderByVersionDesc()
                .map(userDPAConsentDetails -> new UserDPAConsent(userDPAConsentDetails.getConsentText()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Boolean> hasConsentGiven(final String accountId) {
        return getCurrentAccount()
                .flatMap(userAccount -> userConsentAuditLogRepository
                        .findFirstByUserIdOrderByUpdatedOnDesc(userAccount.getUserId())
                        .doOnNext(userDPAConsentAuditLog -> LOGGER.info("User {} most recent consent log Id: {}", userAccount.getUserId(),
                                userDPAConsentAuditLog.getConsentId())))
                .map(userDPAConsentAuditLog -> userDPAConsentAuditLog.getConsentType() == GIVEN)
                .switchIfEmpty(just(FALSE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<UserDPAConsentAuditLog> giveConsent(final String accountId) {
        return executeConsentAction(GIVEN);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<UserDPAConsentAuditLog> revokeConsent(final String accountId) {
        return executeConsentAction(REVOKED);
    }

    private Mono<UserAccount> getCurrentAccount() {
        return currentUserId()
                .flatMap(userAccountPersistenceService::getUserAccountByAuthUserAccountId)
                .map(AuthUserAccount::getUserAccount);
    }

    private Mono<UserDPAConsentAuditLog> executeConsentAction(final UserDPAConsentType userDPAConsentType) {
        return getCurrentAccount()
                .flatMap(userAccount -> getConsent()
                        .map(userDPAConsentDetails -> buildConsentAudiLog(userAccount.getUserId(),
                                userDPAConsentDetails.getConsentId(),
                                userDPAConsentType)))
                .flatMap(userConsentAuditLogRepository::save);
    }

    private UserDPAConsentAuditLog buildConsentAudiLog(final String userId,
                                                       final String consentId,
                                                       final UserDPAConsentType userDPAConsentType) {
        return new UserDPAConsentAuditLog(
                userId,
                consentId,
                userDPAConsentType);
    }

    private Mono<UserDPAConsentDetails> getConsent() {
        return userConsentDetailsRepository.findFirstByOrderByVersionDesc();
    }
}
