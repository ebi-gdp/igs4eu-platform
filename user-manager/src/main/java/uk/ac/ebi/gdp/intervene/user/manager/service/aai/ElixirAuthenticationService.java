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

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.gdp.intervene.user.manager.auth.SecurityContext;
import uk.ac.ebi.gdp.intervene.user.manager.dto.UserInfoDTO;
import uk.ac.ebi.gdp.intervene.user.manager.model.IUserInfo;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public class ElixirAuthenticationService implements IAuthenticationService {

    private final SecurityContext securityContext;
    private final RestTemplate restTemplate;
    private final URI requestURI;

    public ElixirAuthenticationService(final SecurityContext securityContext,
                                       final RestTemplate restTemplate,
                                       final URL requestURL) throws URISyntaxException {
        this.securityContext = securityContext;
        this.restTemplate = restTemplate;
        this.requestURI = requestURL.toURI();
    }

    @Override
    public IUserInfo userInfo() {
        return getUserInfo(securityContext.getAccessToken());
    }

    private IUserInfo getUserInfo(final String accessToken) {
        // LOGGER.debug("ega access token: {}", egaAccessToken);

        final HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION, "Bearer " + accessToken);

        final HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(headers);
        return Objects.requireNonNull(restTemplate
                        .exchange(requestURI.resolve("userinfo"), HttpMethod.GET, entity, UserInfoDTO.class)
                        .getBody(), "Call to /userinfo endpoint received null")
                .toUserInfo();

    }
}
