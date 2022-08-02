/*
 *
 * Copyright 2021 EMBL - European Bioinformatics Institute
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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.gdp.intervene.user.manager.auth.SecurityContext;
import uk.ac.ebi.gdp.intervene.user.manager.service.aai.AuthenticationContext;
import uk.ac.ebi.gdp.intervene.user.manager.service.aai.ElixirAuthenticationService;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

@EnableWebSecurity
public class OAuth2SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http
                .authorizeRequests(auths -> auths
                        .anyRequest()
                        .authenticated())
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
    }

    @Bean
    public ElixirAuthenticationService elixirAuthenticationService(final SecurityContext securityContext,
                                                                   final RestTemplate restTemplate,
                                                                   @Value("${elixir.oidc.url}") final String elixirOidcUrl) throws MalformedURLException, URISyntaxException {
        return new ElixirAuthenticationService(
                securityContext,
                restTemplate,
                new URL(elixirOidcUrl)
        );
    }

    @Bean
    public AuthenticationContext authenticationContext(final SecurityContext securityContext,
                                                       final ElixirAuthenticationService elixirAuthenticationService) {
        return new AuthenticationContext(
                securityContext,
                elixirAuthenticationService
        );
    }

    @Bean
    public SecurityContext securityContext() {
        return new SecurityContext();
    }
}
