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
package uk.ac.ebi.gdp.intervene.user.manager.service.aai;

import uk.ac.ebi.gdp.intervene.user.manager.auth.SecurityContext;

public class AuthenticationContext {

    private final SecurityContext securityContext;
    private final ElixirAuthenticationService elixirAuthenticationService;

    public AuthenticationContext(final SecurityContext securityContext,
                                 final ElixirAuthenticationService elixirAuthenticationService) {
        this.securityContext = securityContext;
        this.elixirAuthenticationService = elixirAuthenticationService;
    }

    public IAuthenticationService getAuthenticationService() {
        return doGetAuthenticationService();
    }

    private IAuthenticationService doGetAuthenticationService() {
        switch (securityContext.getAuthProvider()) {
            case ELIXIR:
            default:
                return elixirAuthenticationService;
        }
    }
}
