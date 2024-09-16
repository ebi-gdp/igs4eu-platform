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
package uk.ac.ebi.gdp.intervene.commons.dpa;

import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.ServerResponse.status;

/**
 * {@code DPAConsentCheck} is a filter that ensures users have given
 * their consent to the Data Processing Agreement (DPA) before allowing access to
 * the platform's APIs. This filter intercepts all incoming requests and checks
 * if the user has provided the required consent. If consent has not been given
 * or has been revoked, the filter responds with a {@code 403 Forbidden} status and
 * prevents further processing of the request.
 *
 * <p>The consent status is typically determined by querying a consent database, ensuring that user
 * consent is verified before proceeding with the request.</p>
 *
 * <p>This filter is essential for compliance with data protection regulations and
 * helps to enforce platform-wide data governance policies.</p>
 */
public class DPAConsentCheck {
    public static final String USER_ACCOUNT_OBJECT = "userAccount";
    private final IUserManagerService userManagerService;

    public DPAConsentCheck(final IUserManagerService userManagerService) {
        this.userManagerService = userManagerService;
    }

    /**
     * Handler filter function to check for user consent.
     */
    public HandlerFilterFunction<ServerResponse, ServerResponse> hasUserGivenConsent() {
        return ((request, next) ->
                userManagerService
                        .getUserAccountDetails()
                        .flatMap(userAccountDTO -> {
                            if ("GIVEN".equalsIgnoreCase(userAccountDTO.consentType())) {
                                request.exchange()
                                        .getAttributes()
                                        .put(USER_ACCOUNT_OBJECT, userAccountDTO);
                                return next.handle(request);  // Proceed with the request
                            } else {
                                return status(FORBIDDEN)
                                        .contentType(APPLICATION_JSON)
                                        .bodyValue("{\"message\": \"User consent is required.\"}");
                            }
                        }));
    }
}
