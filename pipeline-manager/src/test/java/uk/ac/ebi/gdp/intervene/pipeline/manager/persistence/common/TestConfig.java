/*
 *
 * Copyright 2024 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.common;

import io.r2dbc.spi.ConnectionFactory;
import org.mockito.Mockito;
import org.springframework.boot.r2dbc.ConnectionFactoryBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineStatus;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.PipelineDetailsRepository;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.PipelineExecutionStatusRepository;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.repository.PipelineResultRepository;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.service.IPipelinePersistence;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.service.PipelinePersistence;

import java.util.List;

import static org.mockito.Mockito.when;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.common.TestData.USER_ID_ONE;

@TestConfiguration
public class TestConfig extends AbstractR2dbcConfiguration {

    @Bean
    public IPipelinePersistence userAccountPersistenceService(final PipelineDetailsRepository pipelineDetailsRepository,
                                                              final PipelineExecutionStatusRepository pipelineExecutionStatusRepository,
                                                              final PipelineResultRepository pipelineResultRepository) {
        return new PipelinePersistence(
                pipelineDetailsRepository,
                pipelineExecutionStatusRepository,
                pipelineResultRepository);
    }

    @Bean
    @Override
    public ConnectionFactory connectionFactory() {
        return ConnectionFactoryBuilder
                .withUrl("r2dbc:h2:mem:///testdb;DB_CLOSE_DELAY=-1;NON_KEYWORDS=KEY,VALUE")
                .build();
    }

    @Bean("r2dbcDatabaseClient")
    public DatabaseClient databaseClient(final ConnectionFactory connectionFactory) {
        return DatabaseClient
                .builder()
                .connectionFactory(connectionFactory)
                .build();
    }

    @Bean("r2dbcTransactionManager")
    public ReactiveTransactionManager transactionManager(final ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }

    @Bean
    @Override
    public R2dbcCustomConversions r2dbcCustomConversions() {
        return new R2dbcCustomConversions(getStoreConversions(),
                List.of(new PipelineStatusToStringConverter()));
    }

    public static class PipelineStatusToStringConverter implements Converter<PipelineStatus, String> {
        @Override
        public String convert(PipelineStatus pipelineStatus) {
            return pipelineStatus.name();
        }
    }

    @Bean
    public ReactiveAuditorAware<String> auditorAware() {
        final ReactiveAuditorAware<String> reactiveAuditorAware = Mockito.mock(ReactiveAuditorAware.class);
        when(reactiveAuditorAware.getCurrentAuditor()).thenReturn(Mono.just(USER_ID_ONE));
        return reactiveAuditorAware;
    }
}
