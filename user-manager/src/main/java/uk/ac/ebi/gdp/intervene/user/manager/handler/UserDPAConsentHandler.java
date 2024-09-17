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
package uk.ac.ebi.gdp.intervene.user.manager.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.user.manager.dpa.IAuditLogUserDPAConsentService;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;
import static org.springframework.web.reactive.function.server.ServerResponse.status;
import static uk.ac.ebi.gdp.intervene.commons.security.SecurityContextDataProvider.currentUserId;

/**
 * Request handler for User DPA related operations.
 */
public class UserDPAConsentHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDPAConsentHandler.class);
    private final IAuditLogUserDPAConsentService auditLogUserConsentService;

    public UserDPAConsentHandler(final IAuditLogUserDPAConsentService auditLogUserConsentService) {
        this.auditLogUserConsentService = auditLogUserConsentService;
    }

    /**
     * Get DPA consent content.
     *
     * @return DPA consent content in base64 encoded format e.g. PDF.
     */
    public Mono<ServerResponse> getDPAConsentContent() {
        return auditLogUserConsentService
                .fetchDPAContent()
                .flatMap(consentDPA -> ok()
                        .bodyValue(consentDPA.base64EncodedPDFContent())
                );
    }

    /**
     * Record user consent.
     *
     * @return Http status 200 or 201.
     */
    public Mono<ServerResponse> giveConsent() {
        return currentUserId()
                .flatMap(userId -> auditLogUserConsentService
                        .hasConsentGiven(userId)
                        .flatMap(aBoolean -> {
                            if (aBoolean) {
                                LOGGER.info("User {} already has given a consent", userId);
                                return ok()
                                        .build();
                            } else {
                                return auditLogUserConsentService
                                        .giveConsent(userId)
                                        .doOnNext(userConsentAuditLog ->
                                                LOGGER.info("User {} has given a consent, Id: {}", userConsentAuditLog.getUserId(),
                                                        userConsentAuditLog.getConsentId()))
                                        .flatMap(userConsentAuditLog -> status(CREATED)
                                                .build());
                            }
                        }));
    }

    /**
     * Revoke user consent.
     *
     * @return Http status 200 or 201.
     */
    public Mono<ServerResponse> revokeConsent() {
        return currentUserId()
                .flatMap(userId -> auditLogUserConsentService
                        .hasConsentGiven(userId)
                        .flatMap(aBoolean -> {
                            if (aBoolean) {
                                return auditLogUserConsentService
                                        .revokeConsent(userId)
                                        .doOnNext(userConsentAuditLog ->
                                                LOGGER.info("User {} has revoked consent, Id: {}", userConsentAuditLog.getUserId(),
                                                        userConsentAuditLog.getConsentId()))
                                        .flatMap(userConsentAuditLog -> status(CREATED)
                                                .build());
                            } else {
                                LOGGER.info("User {} has already revoked a consent", userId);
                                return ok()
                                        .build();
                            }
                        }));
    }
}
