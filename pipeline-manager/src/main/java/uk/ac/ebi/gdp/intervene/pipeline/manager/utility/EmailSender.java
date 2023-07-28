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
package uk.ac.ebi.gdp.intervene.pipeline.manager.utility;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import reactor.core.publisher.Mono;

import static uk.ac.ebi.gdp.intervene.commons.exception.ServerException.serverException;

public class EmailSender implements IEmailSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailSender.class);
    private final JavaMailSender mailSender;
    private final String emailFrom;

    public EmailSender(final JavaMailSender mailSender,
                       final String emailFrom) {
        this.mailSender = mailSender;
        this.emailFrom = emailFrom;
    }

    public Mono<Void> sendEmailInHTMLFormat(final EmailData emailData) {
        try {
            final MimeMessage message = mailSender.createMimeMessage();
            message.setFrom(new InternetAddress(emailFrom));
            message.setRecipients(MimeMessage.RecipientType.TO, emailData.to());
            message.setSubject(emailData.subject());
            message.setContent(emailData.body(), "text/html; charset=utf-8");
            mailSender.send(message);
            return Mono.empty();
        } catch (final MessagingException messagingException) {
            LOGGER.error("Error while sending message: " + messagingException.getMessage(), messagingException);
            throw serverException(messagingException.getMessage());
        }
    }
}
