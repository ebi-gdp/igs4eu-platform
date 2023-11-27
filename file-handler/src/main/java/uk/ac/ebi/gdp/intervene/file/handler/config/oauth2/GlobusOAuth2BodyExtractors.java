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
package uk.ac.ebi.gdp.intervene.file.handler.config.oauth2;

import org.springframework.http.ReactiveHttpInputMessage;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.web.reactive.function.BodyExtractor;
import reactor.core.publisher.Mono;

/**
 * Body extractor to define static method to get instance of {@link GlobusOAuth2AccessTokenResponseBodyExtractor} response extractor.
 */
public abstract class GlobusOAuth2BodyExtractors {
    /**
     * Extractor to decode an {@link OAuth2AccessTokenResponse}
     *
     * @return a BodyExtractor for {@link OAuth2AccessTokenResponse}
     */
    public static BodyExtractor<Mono<OAuth2AccessTokenResponse>, ReactiveHttpInputMessage> oauth2AccessTokenResponse() {
        return new GlobusOAuth2AccessTokenResponseBodyExtractor();
    }

    private GlobusOAuth2BodyExtractors() {
    }
}
