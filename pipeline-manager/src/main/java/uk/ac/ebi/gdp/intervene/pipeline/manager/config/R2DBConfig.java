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
package uk.ac.ebi.gdp.intervene.pipeline.manager.config;

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import uk.ac.ebi.gdp.intervene.commons.datasource.DatasourceConfigProperties;
import uk.ac.ebi.gdp.intervene.pipeline.manager.constant.GenomeBuild;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.FilesetType;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.DatasetStatusType;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineStatus;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.UserManagerService;

import java.util.List;

import static io.r2dbc.postgresql.client.SSLMode.fromValue;
import static io.r2dbc.postgresql.codec.EnumCodec.builder;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.converter.EnumConverter.FilesetTypeConverter;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.converter.EnumConverter.GenomeBuildConverter;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.converter.EnumConverter.PipelineStatusTypeConverter;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.converter.EnumConverter.DatasetStatusTypeConverter;

/**
 * Reactive database config.
 *
 * @see AbstractR2dbcConfiguration
 * @see ConnectionFactory
 * @see DatabaseClient
 * @see ReactiveTransactionManager
 * @see ReactiveAuditorAware
 */
@EnableTransactionManagement
@Configuration
@EnableR2dbcAuditing
@EnableR2dbcRepositories(basePackages = {"uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository"})
public class R2DBConfig extends AbstractR2dbcConfiguration {

    @Override
    @Bean
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
                        .codecRegistrar(builder().withEnum("pipeline_status", PipelineStatus.class).build())
                        .codecRegistrar(builder().withEnum("fileset_type", FilesetType.class).build())
                        .codecRegistrar(builder().withEnum("genome_build", GenomeBuild.class).build())
                        .codecRegistrar(builder().withEnum("dataset_status_type", DatasetStatusType.class).build())
                        .build());
    }

    @Bean
    @ConfigurationProperties("datasource.pipeline-manager")
    public DatasourceConfigProperties datasourceConfigProperties() {
        return new DatasourceConfigProperties();
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
    @Override
    public R2dbcCustomConversions r2dbcCustomConversions() {
        return new R2dbcCustomConversions(
                getStoreConversions(),
                List.of(new PipelineStatusTypeConverter(),
                        new GenomeBuildConverter(),
                        new FilesetTypeConverter(),
                        new DatasetStatusTypeConverter())
        );
    }

    @Bean
    public ReactiveAuditorAware<String> auditorAware(final UserManagerService userManagerService) {
        return new ReactiveAuditorAwareImpl(userManagerService);
    }
}
