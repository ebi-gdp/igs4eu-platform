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
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.web.util.pattern.PathPatternParser;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Generic OAuth2 security config, provides necessary security config to support
 * modules. Extend this class to declare beans.
 */
public class GenericOAuth2SecurityConfig {

    private static final String actuatorEndpoint = "/actuator/health";

    /**
     * Security filter chain config.
     *
     * @param http {@link ServerHttpSecurity}
     * @param jwkSetURI JWK Set URI
     *
     * @return {@link SecurityWebFilterChain}
     */
    protected SecurityWebFilterChain securityFilterChain(final ServerHttpSecurity http,
                                                         final String jwkSetURI) {
        http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(actuatorEndpoint)//Added default behaviour to expose actuator health endpoint
                        .permitAll()
                        .anyExchange()
                        .authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwtSpec -> jwtSpec.jwtDecoder(reactiveJwtDecoder(jwkSetURI))));
        return http.build();
    }

    /**
     * Basic auth http security config.
     *
     * @param http {@link ServerHttpSecurity}
     * @param patternPath Pattern path to match
     *
     * @return {@link SecurityWebFilterChain}
     */
    protected SecurityWebFilterChain securityFilterChainBasicAuth(final ServerHttpSecurity http,
                                                                  final String patternPath,
                                                                  final HttpMethod httpMethod) {
        final PathPatternParser parser = new PathPatternParser();
        http.csrf((ServerHttpSecurity.CsrfSpec::disable));
        http
                .securityMatcher(new PathPatternParserServerWebExchangeMatcher(parser.parse(patternPath), httpMethod))
                .authorizeExchange((exchanges) -> exchanges
                        .anyExchange()
                        .authenticated())
                .httpBasic(withDefaults());
        return http.build();
    }

    /**
     * JWT decoder.
     *
     * @param jwkSetURI JWK Set URI
     *
     * @return {@link ReactiveJwtDecoder}
     */
    protected ReactiveJwtDecoder reactiveJwtDecoder(final String jwkSetURI) {
        return NimbusReactiveJwtDecoder
                .withJwkSetUri(jwkSetURI)
                .jwtProcessorCustomizer(jwtProcessor -> jwtProcessor
                        .setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(new JOSEObjectType("at+jwt"))))
                .build();
    }

    /**
     * Provides credentials for basic auth.
     *
     * @param username basic auth username
     * @param password basic auth password
     *
     * @return {@link ReactiveUserDetailsService}
     */
    protected ReactiveUserDetailsService userDetailsService(final String username,
                                                            final String password) {
        final UserDetails user = User
                .builder()
                .username(username)
                .password("{noop}%s".formatted(password))
                .roles("USER")
                .build();
        return new MapReactiveUserDetailsService(user);
    }
}
