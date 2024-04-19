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

public interface TargetGenomeFileType {
    String GENO = "geno", PGEN = ".pgen", BED = ".bed";
    String PHENO = "pheno", PSAM = ".psam", FAM = ".fam";
    String VARIANTS = "variants", PVAR = ".pvar", BIM = ".bim";
    String VCF_GZ = ".vcf.gz";

    static String getPropertyName(final String fileName) {
        if (fileName.endsWith(PGEN) || fileName.endsWith(BED)) {
            return GENO;
        } else if (fileName.endsWith(PSAM) || fileName.endsWith(FAM)) {
            return PHENO;
        } else if (fileName.endsWith(PVAR) || fileName.endsWith(BIM)) {
            return VARIANTS;
        } else {
            return "";
        }
    }
}
