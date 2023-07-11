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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.user.manager.dto.UserInfoDTO;
import uk.ac.ebi.gdp.intervene.user.manager.model.IUserInfo;

import java.net.URI;

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static uk.ac.ebi.gdp.intervene.commons.security.SecurityContextDataProvider.currentUserId;

public class ElixirAuthenticationService implements IAuthenticationService {
    private static final Logger LOGGER = getLogger(ElixirAuthenticationService.class);
    private final WebClient webClient;
    private final URI userInfoURI;

    public ElixirAuthenticationService(final WebClient webClient,
                                       final URI userinfoURI) {
        this.webClient = webClient;
        this.userInfoURI = userinfoURI;
    }

    @Override
    public Mono<IUserInfo> userInfo() {
        return currentUserId()
                .doOnNext(currentUserId -> LOGGER.debug("Getting user information for {}", currentUserId))
                .then(getUserInfo());
    }

    private Mono<IUserInfo> getUserInfo() {
        return webClient
                .get()
                .uri(userInfoURI.getPath())
                .accept(APPLICATION_JSON)
                .retrieve()
                .bodyToMono(UserInfoDTO.class)
                .map(UserInfoDTO::toUserInfo);
    }
}
