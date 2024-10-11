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
package uk.ac.ebi.gdp.intervene.pipeline.manager.dto.validation;

import org.springframework.validation.Validator;
import uk.ac.ebi.gdp.intervene.commons.dto.validation.AbstractValidator;
import uk.ac.ebi.gdp.intervene.pipeline.manager.dto.ScoreIdsDTO;

/**
 * A validator class for validating {@code ScoreIdsDTO} objects.
 * This class extends the {@code AbstractValidator} to provide validation logic
 * to {@code ScoreIdsDTO} using the Spring {@code Validator} framework.
 * It ensures that incoming {@code ScoreIdsDTO} objects meet the validation
 * constraints before further processing.
 */
public class ScoreIdsDTOValidator extends AbstractValidator<ScoreIdsDTO, Validator> {
    public ScoreIdsDTOValidator(final Validator validator) {
        super(ScoreIdsDTO.class, validator);
    }
}
