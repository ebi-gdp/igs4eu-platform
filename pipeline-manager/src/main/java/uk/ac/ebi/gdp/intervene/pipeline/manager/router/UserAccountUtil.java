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
package uk.ac.ebi.gdp.intervene.pipeline.manager.router;

import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.commons.dto.usermanager.UserAccountDTO;

import static reactor.core.publisher.Mono.just;
import static uk.ac.ebi.gdp.intervene.commons.dpa.DPAConsentCheck.USER_ACCOUNT_OBJECT;
import static uk.ac.ebi.gdp.intervene.commons.exception.ServerException.serverException;

public interface UserAccountUtil {

    /**
     * Extracts/ retrieve user account details propagated via {@link ServerRequest}.
     *
     * @param serverRequest {@link ServerRequest}.
     *
     * @return {@link UserAccountDTO}.
     */
    static Mono<UserAccountDTO> userAccount(final ServerRequest serverRequest) {
        if (serverRequest
                .attributes()
                .containsKey(USER_ACCOUNT_OBJECT)) {
            return just((UserAccountDTO) serverRequest
                    .attributes()
                    .get(USER_ACCOUNT_OBJECT));
        } else {
            throw serverException(500, "Unable to populate user account details");
        }
    }
}
