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

import org.slf4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.pipeline.manager.dto.PipelineStatusDTO;
import uk.ac.ebi.gdp.intervene.pipeline.manager.exception.MailException;
import uk.ac.ebi.gdp.intervene.pipeline.manager.message.PipelineResultEvent;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineExecutionStatus;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.r2dbc.entity.PipelineStatus;
import uk.ac.ebi.gdp.intervene.pipeline.manager.persistence.service.IPipelinePersistence;
import uk.ac.ebi.gdp.intervene.pipeline.manager.service.UserManagerService;
import uk.ac.ebi.gdp.intervene.pipeline.manager.utility.IEmailSender;

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

/**
 * CSC pipeline handler for EBI & CSC integration hybrid model.
 */
public class PipelineStatusHandler {
    private static final Logger LOGGER = getLogger(PipelineStatusHandler.class);
    private final UserManagerService userManagerService;
    private final IPipelinePersistence pipelinePersistence;
    private final IEmailSender emailService;
    private final String platformURL;

    public PipelineStatusHandler(final UserManagerService userManagerService,
                                 final IPipelinePersistence pipelinePersistence,
                                 final IEmailSender emailService,
                                 final String platformURL) {
        this.userManagerService = userManagerService;
        this.pipelinePersistence = pipelinePersistence;
        this.emailService = emailService;
        this.platformURL = platformURL;
    }

    /**
     * Update pipeline status.
     *
     * @param serverRequest represents a server-side HTTP request, as handled by a {@code HandlerFunction}
     *
     * @return pipeline status represented by {@link PipelineStatusDTO}
     */
    @Transactional(rollbackFor = Exception.class)
    public Mono<ServerResponse> updatePipelineStatus(final ServerRequest serverRequest) {
        final String pipelineId = serverRequest.pathVariable("pipelineId");
        return serverRequest
                .bodyToMono(PipelineStatusDTO.class)
                .flatMap(pipelineStatusDTO -> updatePipelineStatus(pipelineId, pipelineStatusDTO))
                .then(Mono.defer(() -> ok().build()))
                .doOnSuccess(pipelineStatusDTO -> LOGGER.info("Pipeline status has been updated for Pipeline Id: {}", pipelineId));
    }

    /**
     * Update pipeline status.
     *
     * @param pipelineId pipeline id
     * @param pipelineStatusDTO {@link PipelineStatusDTO}
     *
     * @return {@link Void}
     */
    @Transactional(noRollbackFor = MailException.class)
    public Mono<Void> updatePipelineStatus(final String pipelineId,
                                           final PipelineStatusDTO pipelineStatusDTO) {
        LOGGER.info("Pipeline status is being updated to status: {} for Pipeline Id: {}", pipelineStatusDTO.getStatus(), pipelineId);
        return pipelinePersistence
                .updatePipelineStatus(pipelineId, pipelineStatusDTO)
                .flatMap(pipelineExecutionStatus -> handlePipelineOutcome(pipelineExecutionStatus.getStatus(), pipelineExecutionStatus));
    }

    private Mono<Void> handlePipelineOutcome(final PipelineStatus pipelineStatus,
                                             final PipelineExecutionStatus pipelineExecutionStatus) {
        switch (pipelineStatus) {
            case COMPLETED -> {
                LOGGER.trace("Executing block for COMPLETED case");
                final PipelineResultEvent pipelineResultEvent = new PipelineResultEvent(
                        "",
                        pipelineExecutionStatus.getId(),
                        "");
                return pipelinePersistence
                        .createPipelineResult(pipelineResultEvent)
                        .doOnError(throwable -> LOGGER.error("Error occurred while creating Pipeline Result record: {}", throwable.getMessage()))
                        .flatMap(pipelineResult -> buildSuccessEmailData(pipelineResult.getPipelineId(),
                                pipelineExecutionStatus.getPipelineDetails().getUserId()))
                        .flatMap(this::sendEmailInHTMLFormat);
            }
            case ERROR -> {
                LOGGER.trace("Executing block for ERROR case");
                return buildErrorEmailData(pipelineExecutionStatus.getId(), pipelineExecutionStatus.getPipelineDetails().getUserId(),
                        pipelineExecutionStatus.getTraceName(), pipelineExecutionStatus.getTraceExit())
                        .flatMap(this::sendEmailInHTMLFormat);
            }
            default -> {
                LOGGER.trace("Executing block for default case");
                return Mono.empty();
            }
        }
    }

    private Mono<Void> sendEmailInHTMLFormat(final IEmailSender.EmailData emailData) {
        return emailService
                .sendEmailInHTMLFormat(emailData)
                .doOnSuccess(unused -> LOGGER.info("Email sent in HTML format"));
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
                                "<br/><br/>INTERVENE Team"));
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
                                        errorMessage(traceName, traceExit))));
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
