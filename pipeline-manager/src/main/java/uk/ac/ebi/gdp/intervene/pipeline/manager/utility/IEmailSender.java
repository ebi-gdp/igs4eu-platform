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

import reactor.core.publisher.Mono;

/**
 * Email service abstraction.
 */
public interface IEmailSender {
    /**
     * Sends email in HTML format, implement logic accordingly.
     *
     * @param emailData message to be sent
     *
     * @return {@link Void}
     */
    Mono<Void> sendEmailInHTMLFormat(EmailData emailData);

    /**
     * Email record.
     *
     * @param to recipient address
     * @param subject email subject
     * @param body email body
     */
    record EmailData(String to, String subject, String body) {

        @Override
        public String toString() {
            return "EmailData {" +
                    "to='" + to + '\'' +
                    ", subject='" + subject + '\'' +
                    ", body='" + body + '\'' +
                    '}';
        }
    }
}
