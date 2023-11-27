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

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.pipeline.manager.dto.PipelineStatusDTO;
import uk.ac.ebi.gdp.intervene.pipeline.manager.message.PipelineResultEvent;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineExecutionStatus;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineStatus;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.service.IPipelinePersistence;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.UserManagerService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.utility.IEmailSender;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

public class CSCPipelineHandler {
    private final UserManagerService userManagerService;
    private final IPipelinePersistence pipelinePersistence;
    private final IEmailSender emailService;
    private final String platformURL;

    public CSCPipelineHandler(final UserManagerService userManagerService,
                              final IPipelinePersistence pipelinePersistence,
                              final IEmailSender emailService,
                              final String platformURL) {
        this.userManagerService = userManagerService;
        this.pipelinePersistence = pipelinePersistence;
        this.emailService = emailService;
        this.platformURL = platformURL;
    }

    public Mono<ServerResponse> updatePipelineStatus(final ServerRequest serverRequest) {
        return serverRequest
                .bodyToMono(PipelineStatusDTO.class)
                .flatMap(pipelineStatusDTO -> pipelinePersistence
                        .updatePipelineStatus(serverRequest.pathVariable("pipelineId"),
                                pipelineStatusDTO))
                .flatMap(pipelineExecutionStatus -> handlePipelineOutcome(pipelineExecutionStatus.getStatus(), pipelineExecutionStatus))
                .flatMap(pipelineStatusDTO -> ok().build());
    }

    private Mono<Void> handlePipelineOutcome(final PipelineStatus pipelineStatus,
                                             final PipelineExecutionStatus pipelineExecutionStatus) {
        switch (pipelineStatus) {
            case COMPLETED -> {
                final PipelineResultEvent pipelineResultEvent = new PipelineResultEvent(
                        "",
                        pipelineExecutionStatus.getId(),
                        "");
                return pipelinePersistence
                        .persistPipelineResult(pipelineResultEvent)
                        .flatMap(pipelineResult -> buildSuccessEmailData(pipelineResult.getPipelineId(),
                                pipelineExecutionStatus.getPipelineDetails().getUserId()))
                        .flatMap(emailService::sendEmailInHTMLFormat);
            }
            case ERROR -> {
                return buildErrorEmailData(pipelineExecutionStatus.getId(), pipelineExecutionStatus.getPipelineDetails().getUserId(),
                        pipelineExecutionStatus.getTraceName(), pipelineExecutionStatus.getTraceExit())
                        .flatMap(emailService::sendEmailInHTMLFormat);
            }
            default -> {
                return Mono.empty();
            }
        }
    }

    private Mono<IEmailSender.EmailData> buildSuccessEmailData(final String pipelineId,
                                                               final String userId) {
        return userManagerService
                .getUserAccountDetails(userId)
                .map(userAccountDTO -> new IEmailSender.EmailData(
                        userAccountDTO.emailId(),
                        "Result for Pipeline %s".formatted(pipelineId),
                        "Dear %s %s, <br/><br/>Pipeline %s has been completed successfully!"
                                .formatted(userAccountDTO.givenName(),
                                        userAccountDTO.familyName(),
                                        pipelineId) +
                                "<br><br>Please find download link to the result files" +
                                "<br><br><a href=" + platformURL.formatted(pipelineId) + ">Download files</a>" +
                                "<br/><br/>You can always find the most recent report under \"PGS Calculator\" => \"Download most recent results\". <br/><br/>INTERVENE Team")
                );
    }

    private Mono<IEmailSender.EmailData> buildErrorEmailData(final String pipelineId,
                                                             final String userId,
                                                             final String traceName,
                                                             final byte traceExit) {
        return userManagerService
                .getUserAccountDetails(userId)
                .map(userAccountDTO -> new IEmailSender.EmailData(
                        userAccountDTO.emailId(),
                        "Error running pipeline %s".formatted(pipelineId),
                        "Dear %s %s,<br/><br/>Your pipeline instance %s has failed.<br/><br/>Please find error message<br/>%s<br/><br/>INTERVENE Team"
                                .formatted(userAccountDTO.givenName(),
                                        userAccountDTO.familyName(),
                                        pipelineId,
                                        errorMessage(traceName, traceExit)
                                )));
    }

    private String errorMessage(final String traceName,
                                final byte traceExit) {
        return "<html>" +
                "<head>" +
                "<style>" +
                "table, th, td {" +
                " border: 1px solid #FF0000;" +
                " border-collapse: collapse;" +
                " text-align: left;" +
                " padding: 10px" +
                "}" +
                "</style>" +
                "</head>" +
                "<table>" +
                "<tr><th>Flag</th><th>Value</th></tr>" +
                "<tr><td>Status</td><td>Error</td></tr>" +
                "<tr><td>Trace name</td><td>%s</td></tr>".formatted(traceName) +
                "<tr><td>Trace exit</td><td>%s</td></tr>".formatted(traceExit) +
                "</table>";
    }
}
