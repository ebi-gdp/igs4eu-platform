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
package uk.ac.ebi.gdp.intervene.pipeline.manager.constant;

import static uk.ac.ebi.gdp.intervene.pipeline.manager.router.validation.FileValidations.FileExtension.BED;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.router.validation.FileValidations.FileExtension.BIM;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.router.validation.FileValidations.FileExtension.BIM_ZST;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.router.validation.FileValidations.FileExtension.FAM;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.router.validation.FileValidations.FileExtension.PGEN;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.router.validation.FileValidations.FileExtension.PSAM;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.router.validation.FileValidations.FileExtension.PVAR;
import static uk.ac.ebi.gdp.intervene.pipeline.manager.router.validation.FileValidations.FileExtension.PVAR_ZST;

public interface TargetGenomeFileType {
    String GENO = "geno";
    String PHENO = "pheno";
    String VARIANTS = "variants";

    static String getPropertyName(final String fileName) {
        if (fileName.endsWith(PGEN.getFileExtension()) || fileName.endsWith(BED.getFileExtension())) {
            return GENO;
        } else if (fileName.endsWith(PSAM.getFileExtension()) || fileName.endsWith(FAM.getFileExtension())) {
            return PHENO;
        } else if (fileName.endsWith(PVAR.getFileExtension()) || fileName.endsWith(PVAR_ZST.getFileExtension())
                || fileName.endsWith(BIM.getFileExtension()) || fileName.endsWith(BIM_ZST.getFileExtension())) {
            return VARIANTS;
        } else {
            return "";
        }
    }
}
