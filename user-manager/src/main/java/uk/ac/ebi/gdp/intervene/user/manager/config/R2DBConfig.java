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
package uk.ac.ebi.gdp.intervene.user.manager.config;

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import uk.ac.ebi.gdp.intervene.commons.datasource.DatasourceConfigProperties;
import uk.ac.ebi.gdp.intervene.commons.security.AuthProviderType;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.AuthUserAccountStatus;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.UserAccountStatus;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.UserDPAConsentType;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.repository.mapper.AuthUserAccountEntityMapper;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.service.IUserAccountPersistenceService;

import java.util.List;

import static io.r2dbc.postgresql.client.SSLMode.fromValue;
import static io.r2dbc.postgresql.codec.EnumCodec.builder;
import static uk.ac.ebi.gdp.intervene.user.manager.converter.EnumConverter.AuthProviderWritingConverter;
import static uk.ac.ebi.gdp.intervene.user.manager.converter.EnumConverter.AuthUserAccountStatusWritingTypeConverter;
import static uk.ac.ebi.gdp.intervene.user.manager.converter.EnumConverter.ConsentTypeWritingConverter;
import static uk.ac.ebi.gdp.intervene.user.manager.converter.EnumConverter.UserAccountStatusTypeWritingConverter;

/**
 * Reactive database config.
 *
 * @see AbstractR2dbcConfiguration
 * @see ConnectionFactory
 * @see DatabaseClient
 * @see ReactiveTransactionManager
 * @see AuthUserAccountEntityMapper
 */
@EnableTransactionManagement
@Configuration
@EnableR2dbcAuditing
@EnableR2dbcRepositories(basePackages = {"uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.repository"})
public class R2DBConfig extends AbstractR2dbcConfiguration {

    @Bean
    @Override
    public ConnectionFactory connectionFactory() {
        final DatasourceConfigProperties datasourceConfigProperties = datasourceConfigProperties();
        return new PostgresqlConnectionFactory(
                PostgresqlConnectionConfiguration.builder()
                        .host(datasourceConfigProperties.getHost())
                        .port(datasourceConfigProperties.getPort())
                        .username(datasourceConfigProperties.getUsername())
                        .password(datasourceConfigProperties.getPassword())
                        .database(datasourceConfigProperties.getDatabase())
                        .schema(datasourceConfigProperties.getSchema())
                        .sslMode(fromValue(datasourceConfigProperties.getSslMode()))
                        .codecRegistrar(builder().withEnum("auth_user_account_provider", AuthProviderType.class).build())
                        .codecRegistrar(builder().withEnum("auth_user_account_status", AuthUserAccountStatus.class).build())
                        .codecRegistrar(builder().withEnum("user_account_status", UserAccountStatus.class).build())
                        .codecRegistrar(builder().withEnum("user_dpa_consent_type", UserDPAConsentType.class).build())
                        .build());
    }

    @Bean
    @ConfigurationProperties("datasource.user-manager")
    public DatasourceConfigProperties datasourceConfigProperties() {
        return new DatasourceConfigProperties();
    }

    /**
     * @return list of custom converters
     * @see AuthProviderWritingConverter
     * @see AuthUserAccountStatusWritingTypeConverter
     * @see UserAccountStatusTypeWritingConverter
     * @see ConsentTypeWritingConverter
     */
    @Override
    protected List<Object> getCustomConverters() {
        return List.of(
                new AuthProviderWritingConverter(),
                new AuthUserAccountStatusWritingTypeConverter(),
                new UserAccountStatusTypeWritingConverter(),
                new ConsentTypeWritingConverter()
        );
    }

    @Bean("r2dbcDatabaseClient")
    public DatabaseClient r2dbcDatabaseClient(final ConnectionFactory connectionFactory) {
        return DatabaseClient.builder()
                .connectionFactory(connectionFactory)
                .bindMarkers(getDialect(connectionFactory).getBindMarkersFactory())
                .build();
    }

    @Bean("r2dbcTransactionManager")
    public ReactiveTransactionManager r2dbcTransactionManager(final ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }

    @Bean
    public ReactiveAuditorAware<String> auditorAware(final IUserAccountPersistenceService userAccountPersistenceService) {
        return new ReactiveAuditorAwareImpl(userAccountPersistenceService);
    }
}
