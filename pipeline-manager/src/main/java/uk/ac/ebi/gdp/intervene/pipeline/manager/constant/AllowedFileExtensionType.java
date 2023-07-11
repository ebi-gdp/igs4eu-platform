/*
 *
 * Copyright 2022 EMBL - European Bioinformatics Institute
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

import org.apache.commons.io.FilenameUtils;

import java.util.Optional;

public interface AllowedFileExtensionType {

    static String getFileNameWithoutExtension(final String fileName) {
        return FilenameUtils.getBaseName(fileName);
    }

    /*static boolean isFileExtensionBFilePath(final String fileName) {
        return FileExtension.containsFileExtension(fileName);
    }*/

    static boolean isFileExtensionPFilePath(final String fileName) {
        return FilenameUtils.getExtension(fileName).startsWith("p");
    }

    static boolean isFileExtensionVCFPath(final String fileName) {
        return fileName.endsWith(".vcf.gz");
    }

    /*static boolean isAllowedFileExtension(final String fileName) {
        return isFileExtensionBFilePath(fileName) || isFileExtensionPFilePath(fileName) || isFileExtensionVCFPath(fileName);
    }*/

    enum FileExtension {
        BED("bed"),
        BIM("bim"),
        FAM("fam"),
        PGEN("pgen"),
        PSAM("psam"),
        PVAR("pvar"),
        VCF_PATH("vcf_path");

        private final String fileExtension;

        FileExtension(final String fileExtension) {
            this.fileExtension = fileExtension;
        }

        public String getFileExtension() {
            return fileExtension;
        }

        public static Optional<String> getKey(final String fileExtensionToBeChecked) {
            for (final FileExtension fileExtensionType : FileExtension.values()) {
                if (fileExtensionToBeChecked.toLowerCase().endsWith(fileExtensionType.getFileExtension())) {
                    return Optional.of(fileExtensionType.getFileExtension());
                }
            }
            return Optional.empty();
        }

        /*public static boolean containsFileExtension(final String fileExtensionToBeChecked) {
            for (final BFileExtension fileExtensionType : BFileExtension.values()) {
                if (fileExtensionToBeChecked.toLowerCase().endsWith(fileExtensionType.getFileExtension())) {
                    return true;
                }
            }
            return false;
        }*/
    }
}
