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
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.springframework.web.reactive.function.BodyExtractors.toMono;
import static reactor.core.publisher.Mono.error;

/**
 * Token response body extractor, parse & extract the Globus access token from the response received
 * from Globus authentication endpoint
 *
 * @see BodyExtractor
 * @see OAuth2AccessTokenResponse
 * @see ReactiveHttpInputMessage
 */
class GlobusOAuth2AccessTokenResponseBodyExtractor implements BodyExtractor<Mono<OAuth2AccessTokenResponse>, ReactiveHttpInputMessage> {
    private static final String INVALID_TOKEN_RESPONSE_ERROR_CODE = "invalid_token_response";

    private static final ParameterizedTypeReference<Map<String, Object>> STRING_OBJECT_MAP = new ParameterizedTypeReference<>() {
    };

    GlobusOAuth2AccessTokenResponseBodyExtractor() {
    }

    /**
     * @see OAuth2AccessTokenResponse
     * @see OAuth2AuthorizationException
     * @see GlobusOAuth2AccessTokenResponseBodyExtractor
     */
    @Override
    public Mono<OAuth2AccessTokenResponse> extract(final ReactiveHttpInputMessage inputMessage,
                                                   final Context context) {
        final BodyExtractor<Mono<Map<String, Object>>, ReactiveHttpInputMessage> delegate = toMono(STRING_OBJECT_MAP);
        return delegate.extract(inputMessage, context)
                .onErrorMap((ex) -> new OAuth2AuthorizationException(
                        invalidTokenResponse("An error occurred parsing the Access Token response: " + ex.getMessage()),
                        ex))
                .switchIfEmpty(error(() -> new OAuth2AuthorizationException(
                        invalidTokenResponse("Empty OAuth 2.0 Access Token Response"))))
                .map(this::parse)
                .flatMap(this::oauth2AccessTokenResponse)
                .map(this::oauth2AccessTokenResponse);
    }

    protected TokenResponse parse(final Map<String, Object> json) {
        try {
            return TokenResponse.parse(extractTransferAPIAccessToken(json));
        } catch (ParseException ex) {
            OAuth2Error oauth2Error = invalidTokenResponse(
                    "An error occurred parsing the Access Token response: " + ex.getMessage());
            throw new OAuth2AuthorizationException(oauth2Error, ex);
        }
    }

    @SuppressWarnings("unchecked")
    protected JSONObject extractTransferAPIAccessToken(final Map<String, Object> json) {
        final Object otherTokens = ((List<?>) json.get("other_tokens")).get(0);
        return new JSONObject((Map<String, ?>) otherTokens);
    }

    protected OAuth2Error invalidTokenResponse(final String message) {
        return new OAuth2Error(INVALID_TOKEN_RESPONSE_ERROR_CODE, message, null);
    }

    protected Mono<AccessTokenResponse> oauth2AccessTokenResponse(final TokenResponse tokenResponse) {
        if (tokenResponse.indicatesSuccess()) {
            return Mono.just(tokenResponse).cast(AccessTokenResponse.class);
        }
        final TokenErrorResponse tokenErrorResponse = (TokenErrorResponse) tokenResponse;
        final ErrorObject errorObject = tokenErrorResponse.getErrorObject();
        final OAuth2Error oauth2Error = getOAuth2Error(errorObject);
        return error(new OAuth2AuthorizationException(oauth2Error));
    }

    protected OAuth2Error getOAuth2Error(final ErrorObject errorObject) {
        if (errorObject == null) {
            return new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR);
        }
        final String code = (errorObject.getCode() != null) ? errorObject.getCode() : OAuth2ErrorCodes.SERVER_ERROR;
        final String description = errorObject.getDescription();
        final String uri = (errorObject.getURI() != null) ? errorObject.getURI().toString() : null;
        return new OAuth2Error(code, description, uri);
    }

    protected OAuth2AccessTokenResponse oauth2AccessTokenResponse(final AccessTokenResponse accessTokenResponse) {
        final AccessToken accessToken = accessTokenResponse.getTokens().getAccessToken();
        OAuth2AccessToken.TokenType accessTokenType = null;
        if (OAuth2AccessToken.TokenType.BEARER.getValue().equalsIgnoreCase(accessToken.getType().getValue())) {
            accessTokenType = OAuth2AccessToken.TokenType.BEARER;
        }
        final long expiresIn = accessToken.getLifetime();
        final Set<String> scopes = (accessToken.getScope() != null)
                ? new LinkedHashSet<>(accessToken.getScope().toStringList()) : Collections.emptySet();
        String refreshToken = null;
        if (accessTokenResponse.getTokens().getRefreshToken() != null) {
            refreshToken = accessTokenResponse.getTokens().getRefreshToken().getValue();
        }
        final Map<String, Object> additionalParameters = new LinkedHashMap<>(accessTokenResponse.getCustomParameters());
        return OAuth2AccessTokenResponse
                .withToken(accessToken.getValue())
                .tokenType(accessTokenType)
                .expiresIn(expiresIn)
                .scopes(scopes)
                .refreshToken(refreshToken)
                .additionalParameters(additionalParameters)
                .build();
    }
}
