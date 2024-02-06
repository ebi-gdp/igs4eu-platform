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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import uk.ac.ebi.gdp.intervene.commons.security.AuthProviderType;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.AuthUserAccountStatus;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.entity.UserAccountStatus;
import uk.ac.ebi.gdp.intervene.user.manager.persistence.r2dbc.repository.mapper.AuthUserAccountEntityMapper;

import java.util.List;

import static io.r2dbc.postgresql.client.SSLMode.fromValue;
import static io.r2dbc.postgresql.codec.EnumCodec.builder;
import static uk.ac.ebi.gdp.intervene.user.manager.converter.EnumConverter.AuthProviderWritingConverter;
import static uk.ac.ebi.gdp.intervene.user.manager.converter.EnumConverter.AuthUserAccountStatusTypeConverter;
import static uk.ac.ebi.gdp.intervene.user.manager.converter.EnumConverter.UserAccountStatusTypeConverter;

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

    @Value("${datasource.user-manager.host}")
    private String dbHost;

    @Value("${datasource.user-manager.port}")
    private int port;

    @Value("${datasource.user-manager.username}")
    private String dbUsername;

    @Value("${datasource.user-manager.password}")
    private String password;

    @Value("${datasource.user-manager.database}")
    private String database;

    @Value("${datasource.user-manager.schema}")
    private String schema;

    @Value("${datasource.user-manager.ssl-mode}")
    private String sslMode;

    @Bean
    @Override
    public ConnectionFactory connectionFactory() {
        return new PostgresqlConnectionFactory(
                PostgresqlConnectionConfiguration.builder()
                        .host(dbHost)
                        .port(port)
                        .username(dbUsername)
                        .password(password)
                        .database(database)
                        .schema(schema)
                        .sslMode(fromValue(sslMode))
                        .codecRegistrar(builder().withEnum("auth_user_account_provider", AuthProviderType.class).build())
                        .codecRegistrar(builder().withEnum("auth_user_account_status", AuthUserAccountStatus.class).build())
                        .codecRegistrar(builder().withEnum("user_account_status", UserAccountStatus.class).build())
                        .build());
    }

    /**
     * @return list of custom converters
     * @see AuthProviderWritingConverter
     * @see AuthUserAccountStatusTypeConverter
     * @see UserAccountStatusTypeConverter
     */
    @Override
    protected List<Object> getCustomConverters() {
        return List.of(
                new AuthProviderWritingConverter(),
                new AuthUserAccountStatusTypeConverter(),
                new UserAccountStatusTypeConverter()
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
}
