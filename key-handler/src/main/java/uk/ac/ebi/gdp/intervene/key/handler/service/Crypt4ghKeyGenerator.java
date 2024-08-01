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
package uk.ac.ebi.gdp.intervene.key.handler.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import uk.ac.ebi.gdp.intervene.key.handler.cryptography.Crypt4ghKeygen;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static reactor.core.publisher.Mono.fromCallable;
import static reactor.core.scheduler.Schedulers.boundedElastic;
import static uk.ac.ebi.gdp.intervene.key.handler.service.IKeyGenerator.KeyGeneratorStatus.FAILURE;
import static uk.ac.ebi.gdp.intervene.key.handler.service.IKeyGenerator.KeyGeneratorStatus.SUCCESS;

/**
 * Implementation of {@link IKeyGenerator} for generating cryptographic key pairs using the Crypt4gh tool.
 * This class handles the generation of both private and public keys and manages the process using the specified
 * binary path and shell commands.
 */
public class Crypt4ghKeyGenerator implements IKeyGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(Crypt4ghKeyGenerator.class);
    private final Crypt4ghKeygen crypt4ghKeygen;
    private final List<String> shellInterpreterCmd;

    public Crypt4ghKeyGenerator(final Crypt4ghKeygen crypt4ghKeygen,
                                final List<String> shellInterpreterCmd) {
        this.crypt4ghKeygen = crypt4ghKeygen;
        this.shellInterpreterCmd = shellInterpreterCmd;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<KeyGeneratorStatus> generate(final Path privateKeyPath,
                                             final Path publicKeyPath) {
        final ProcessBuilder processBuilder = processBuilder(crypt4ghKeygen
                .crypt4ghGenerateKeysBashCmd(
                        privateKeyPath,
                        publicKeyPath));
        return runCommand(processBuilder);
    }

    private static Mono<KeyGeneratorStatus> runCommand(final ProcessBuilder processBuilder) {
        return fromCallable(() -> {
            try {
                final int exitCode = processBuilder
                        .start()
                        .waitFor();
                if (exitCode == 0) {
                    LOGGER.info("Private/Public key value pair has been generated!");
                } else {
                    LOGGER.error("Error! Process finished with exit-code: {}", exitCode);
                    return FAILURE;
                }
            } catch (IOException | InterruptedException e) {
                LOGGER.error("Error occurred while waiting for process: {}", e.getMessage(), e);
                return FAILURE;
            }
            return SUCCESS;
        }).subscribeOn(boundedElastic());
    }

    private ProcessBuilder processBuilder(final String crypt4ghBashCmd) {
        final ProcessBuilder processBuilder = new ProcessBuilder();
        final List<String> crypt4ghExecutableCmds = new ArrayList<>(shellInterpreterCmd);
        crypt4ghExecutableCmds.add(String.join(" ", shellInterpreterCmd) + " " + crypt4ghBashCmd);
        processBuilder.command(crypt4ghExecutableCmds);
        return processBuilder;
    }
}
