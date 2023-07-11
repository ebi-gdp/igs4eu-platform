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

import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import net.minidev.json.JSONObject;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ReactiveHttpInputMessage;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.web.reactive.function.BodyExtractor;
import org.springframework.web.reactive.function.BodyExtractors;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class GlobusOAuth2AccessTokenResponseBodyExtractor implements BodyExtractor<Mono<OAuth2AccessTokenResponse>, ReactiveHttpInputMessage> {
    private static final String INVALID_TOKEN_RESPONSE_ERROR_CODE = "invalid_token_response";

    private static final ParameterizedTypeReference<Map<String, Object>> STRING_OBJECT_MAP = new ParameterizedTypeReference<Map<String, Object>>() {
    };

    GlobusOAuth2AccessTokenResponseBodyExtractor() {
    }

    @Override
    public Mono<OAuth2AccessTokenResponse> extract(ReactiveHttpInputMessage inputMessage, Context context) {
        BodyExtractor<Mono<Map<String, Object>>, ReactiveHttpInputMessage> delegate = BodyExtractors
                .toMono(STRING_OBJECT_MAP);
        return delegate.extract(inputMessage, context)
                .onErrorMap((ex) -> new OAuth2AuthorizationException(
                        invalidTokenResponse("An error occurred parsing the Access Token response: " + ex.getMessage()),
                        ex))
                .switchIfEmpty(Mono.error(() -> new OAuth2AuthorizationException(
                        invalidTokenResponse("Empty OAuth 2.0 Access Token Response"))))
                .map(GlobusOAuth2AccessTokenResponseBodyExtractor::parse)
                .flatMap(GlobusOAuth2AccessTokenResponseBodyExtractor::oauth2AccessTokenResponse)
                .map(GlobusOAuth2AccessTokenResponseBodyExtractor::oauth2AccessTokenResponse);
    }

    private static TokenResponse parse(Map<String, Object> json) {
        try {
            return TokenResponse.parse(extractTransferAPIAccessToken(json));
        } catch (ParseException ex) {
            OAuth2Error oauth2Error = invalidTokenResponse(
                    "An error occurred parsing the Access Token response: " + ex.getMessage());
            throw new OAuth2AuthorizationException(oauth2Error, ex);
        }
    }

    @SuppressWarnings("unchecked")
    private static JSONObject extractTransferAPIAccessToken(final Map<String, Object> json) {
        final Object otherTokens = ((List<?>) json.get("other_tokens")).get(0);
        return new JSONObject((Map<String, ?>) otherTokens);
    }

    private static OAuth2Error invalidTokenResponse(String message) {
        return new OAuth2Error(INVALID_TOKEN_RESPONSE_ERROR_CODE, message, null);
    }

    private static Mono<AccessTokenResponse> oauth2AccessTokenResponse(TokenResponse tokenResponse) {
        if (tokenResponse.indicatesSuccess()) {
            return Mono.just(tokenResponse).cast(AccessTokenResponse.class);
        }
        TokenErrorResponse tokenErrorResponse = (TokenErrorResponse) tokenResponse;
        ErrorObject errorObject = tokenErrorResponse.getErrorObject();
        OAuth2Error oauth2Error = getOAuth2Error(errorObject);
        return Mono.error(new OAuth2AuthorizationException(oauth2Error));
    }

    private static OAuth2Error getOAuth2Error(ErrorObject errorObject) {
        if (errorObject == null) {
            return new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR);
        }
        String code = (errorObject.getCode() != null) ? errorObject.getCode() : OAuth2ErrorCodes.SERVER_ERROR;
        String description = errorObject.getDescription();
        String uri = (errorObject.getURI() != null) ? errorObject.getURI().toString() : null;
        return new OAuth2Error(code, description, uri);
    }

    private static OAuth2AccessTokenResponse oauth2AccessTokenResponse(AccessTokenResponse accessTokenResponse) {
        AccessToken accessToken = accessTokenResponse.getTokens().getAccessToken();
        OAuth2AccessToken.TokenType accessTokenType = null;
        if (OAuth2AccessToken.TokenType.BEARER.getValue().equalsIgnoreCase(accessToken.getType().getValue())) {
            accessTokenType = OAuth2AccessToken.TokenType.BEARER;
        }
        long expiresIn = accessToken.getLifetime();
        Set<String> scopes = (accessToken.getScope() != null)
                ? new LinkedHashSet<>(accessToken.getScope().toStringList()) : Collections.emptySet();
        String refreshToken = null;
        if (accessTokenResponse.getTokens().getRefreshToken() != null) {
            refreshToken = accessTokenResponse.getTokens().getRefreshToken().getValue();
        }
        Map<String, Object> additionalParameters = new LinkedHashMap<>(accessTokenResponse.getCustomParameters());
        // @formatter:off
        return OAuth2AccessTokenResponse.withToken(accessToken.getValue())
                .tokenType(accessTokenType)
                .expiresIn(expiresIn)
                .scopes(scopes)
                .refreshToken(refreshToken)
                .additionalParameters(additionalParameters)
                .build();
        // @formatter:on
    }
}
