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
package uk.ac.ebi.gdp.intervene.pipeline.manager.router;

import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.pipeline.manager.message.PipelineResultEvent;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineDetails;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineStatus;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.service.IPipelinePersistence;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.UserManagerService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.utility.IEmailSender;

import static org.springframework.web.reactive.function.server.ServerResponse.status;

public class CSCPipelineHandler {
    private final UserManagerService userManagerService;
    private final IPipelinePersistence pipelinePersistence;
    private final IEmailSender emailService;

    public CSCPipelineHandler(final UserManagerService userManagerService,
                              final IPipelinePersistence pipelinePersistence,
                              final IEmailSender emailService) {
        this.userManagerService = userManagerService;
        this.pipelinePersistence = pipelinePersistence;
        this.emailService = emailService;
    }

    public Mono<ServerResponse> pipelineNotificationCallback(final ServerRequest serverRequest) {
        return serverRequest
                .bodyToMono(PipelineResultEvent.class)
                .flatMap(this::handlePipelineOutcome)
                .flatMap(unused -> status(HttpStatus.ACCEPTED).build());
    }

    public Mono<Void> handlePipelineOutcome(final PipelineResultEvent pipelineResultEvent) {
        return updatePipelineStatus(pipelineResultEvent)
                .flatMap(pipelineDetails -> pipelinePersistence
                        .persistPipelineResult(pipelineResultEvent)
                        .flatMap(pipelineResult -> buildEmailData(pipelineDetails))
                        .flatMap(emailService::sendEmailInHTMLFormat));
    }

    private Mono<PipelineDetails> updatePipelineStatus(final PipelineResultEvent pipelineResultEvent) {
        return pipelinePersistence.updatePipelineDetailsStatus(
                pipelineResultEvent.pipelineId(),
                PipelineStatus.valueOf(pipelineResultEvent.status().toUpperCase())
        );
    }

    private Mono<IEmailSender.EmailData> buildEmailData(final PipelineDetails pipelineDetails) {
        return userManagerService
                .getUserAccountDetails(pipelineDetails.getUserId())
                .map(userAccountDTO -> new IEmailSender.EmailData(
                        userAccountDTO.emailId(),
                        "Result for Pipeline %s".formatted(pipelineDetails.getPipelineId()),
                        "Dear %s %s, <br/><br/>Please find the report at \"PGS Calculator\" => \"Download most recent results\". <br/><br/>INTERVENE Team"
                                .formatted(userAccountDTO.givenName(),
                                        userAccountDTO.familyName()
                                )));
    }
}
