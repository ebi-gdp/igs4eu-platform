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

import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.UserDPAConsentAuditLog;

/**
 * Audit log user DPA consent service interface.
 */
public interface IAuditLogUserDPAConsentService {
    /**
     * Fetch DPA consent.
     *
     * @return {@link UserDPAConsent}.
     */
    Mono<UserDPAConsent> fetchDPAContent();

    /**
     * Check & return whether user has given a consent.
     *
     * @param userId user id for whom consent to be checked.
     *
     * @return boolean value.
     */
    Mono<Boolean> hasConsentGiven(String userId);

    /**
     * Records user consent.
     *
     * @param accountId user id.
     *
     * @return {@link UserDPAConsentAuditLog}.
     */
    Mono<UserDPAConsentAuditLog> giveConsent(String accountId);

    /**
     * Revokes user account.
     *
     * @param accountId user id.
     *
     * @return {@link UserDPAConsentAuditLog}.
     */
    Mono<UserDPAConsentAuditLog> revokeConsent(String accountId);
}
