/*
 *
 * Copyright 2019 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.gdp.intervene.user.manager.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import uk.ac.ebi.gdp.intervene.user.manager.exception.ServiceException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class RestTemplateErrorResponseHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(final ClientHttpResponse clientHttpResponse) throws IOException {
        return (clientHttpResponse.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR
                || clientHttpResponse.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR);
    }

    @Override
    public void handleError(final ClientHttpResponse clientHttpResponse) throws IOException {
        if (clientHttpResponse.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR) {
            throw new ServiceException(clientHttpResponse.getStatusCode(), toString(clientHttpResponse.getBody()));
        } else if (clientHttpResponse.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR) {
            throw new IOException(toString(clientHttpResponse.getBody()));
        }
    }

    private String toString(final InputStream inputStream) throws IOException {
        final StringBuilder output = new StringBuilder();
        try (final Reader reader = new BufferedReader(new InputStreamReader(inputStream,
                Charset.forName(StandardCharsets.UTF_8.name())))) {
            int c;
            while ((c = reader.read()) != -1) {
                output.append((char) c);
            }
            return output.toString();
        }
    }
}
