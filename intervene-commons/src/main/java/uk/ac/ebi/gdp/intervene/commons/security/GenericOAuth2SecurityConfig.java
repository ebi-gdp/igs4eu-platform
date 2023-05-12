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
package uk.ac.ebi.gdp.intervene.commons.security;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.just;
import static uk.ac.ebi.gdp.intervene.commons.exception.ClientException.clientException;
import static uk.ac.ebi.gdp.intervene.commons.exception.ServerException.serverException;

public class GenericOAuth2SecurityConfig {

    protected SecurityWebFilterChain securityFilterChain(final ServerHttpSecurity http,
                                                         final String jwkSetURI) {
        http
                .authorizeExchange(exchanges ->
                        exchanges
                                .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwtSpec -> jwtSpec.jwtDecoder(reactiveJwtDecoder(jwkSetURI))));
        return http.build();
    }

    protected ReactiveJwtDecoder reactiveJwtDecoder(final String jwkSetURI) {
        return NimbusReactiveJwtDecoder
                .withJwkSetUri(jwkSetURI)
                .jwtProcessorCustomizer(jwtProcessor -> jwtProcessor
                        .setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(new JOSEObjectType("at+jwt"))))
                .build();
    }

    protected ExchangeFilterFunction errorHandler() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().is4xxClientError()) {
                return clientResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> error(clientException(clientResponse.statusCode().value(), errorBody)));
            } else if (clientResponse.statusCode().is5xxServerError()) {
                return clientResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> error(serverException(clientResponse.statusCode().value(), errorBody)));
            } else {
                return just(clientResponse);
            }
        });
    }
}
