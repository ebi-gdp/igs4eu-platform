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
package uk.ac.ebi.gdp.intervene.pipeline.manager.config;

import org.springframework.data.domain.ReactiveAuditorAware;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.commons.dto.usermanager.UserAccountDTO;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.UserManagerService;

public class ReactiveAuditorAwareImpl implements ReactiveAuditorAware<String> {
    private final UserManagerService userManagerService;

    public ReactiveAuditorAwareImpl(final UserManagerService userManagerService) {
        this.userManagerService = userManagerService;
    }

    @Override
    public Mono<String> getCurrentAuditor() {
        return userManagerService
                .getUserAccountDetails()
                .map(UserAccountDTO::accountId);
    }
}
