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
package uk.ac.ebi.gdp.intervene.user.manager.config;

import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.security.core.userdetails.User;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.service.IUserAccountPersistenceService;

import static uk.ac.ebi.gdp.intervene.commons.security.SecurityContextDataProvider.currentUserId;
import static uk.ac.ebi.gdp.intervene.commons.security.SecurityContextDataProvider.isPrincipalOfTypeUser;
import static uk.ac.ebi.gdp.intervene.commons.security.SecurityContextDataProvider.user;

/**
 * Implementation of {@link ReactiveAuditorAware},
 * populates current auditor
 */
public class ReactiveAuditorAwareImpl implements ReactiveAuditorAware<String> {
    private final IUserAccountPersistenceService userAccountPersistenceService;

    public ReactiveAuditorAwareImpl(final IUserAccountPersistenceService userAccountPersistenceService) {
        this.userAccountPersistenceService = userAccountPersistenceService;
    }

    /**
     * Determine principal type & extracts user account id.
     *
     * @return user account id.
     */
    @Override
    public Mono<String> getCurrentAuditor() {
        return isPrincipalOfTypeUser()
                .flatMap(aBoolean -> {
                    if (aBoolean) {
                        return user()
                                .map(User::getUsername);
                    } else {
                        return currentUserId()
                                .flatMap(userId -> userAccountPersistenceService
                                        .getUserAccountByAuthUserAccountId(userId)
                                        .map(authUserAccount -> authUserAccount
                                                .getUserAccount()
                                                .getUserId()));
                    }
                });
    }
}
