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
package uk.ac.ebi.gdp.intervene.commons.dto.filehandler;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.io.FilenameUtils;

import java.util.List;

import static java.util.List.copyOf;

public class GlobusFileDetailsWrapperDTO implements IGlobusFileDetailsWrapper {
    @JsonProperty("path")
    private String path;

    @JsonProperty("total")
    private int totalNoOfFiles;

    @JsonProperty("DATA")
    private List<GlobusFileDetails> fileDetailsList;

    private GlobusFileDetailsWrapperDTO() {
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public int getTotalNoOfFiles() {
        return totalNoOfFiles;
    }

    @Override
    public List<GlobusFileDetails> getFileDetailsList() {
        return copyOf(fileDetailsList);
    }

    /*public List<GlobusFileDetails> getData() {
        return copyOf(data);
    }*/

    public static class GlobusFileDetails {
        private String type;
        private long size;
        @JsonProperty("name")
        private String fileName;

        private GlobusFileDetails() {
        }

        public String getType() {
            return type;
        }

        public long getSize() {
            return size;
        }

        public String getFileName() {
            return fileName;
        }

        @JsonIgnore
        public String getFileNameWithoutExtension() {
            return FilenameUtils.getExtension(fileName);
        }

        @JsonIgnore
        public String getProperty() {
            if (fileName.endsWith(".vcf.gz")) {
                return "vcf_path";
            } else {
                return getFileNameWithoutExtension();
            }
        }
    }
}
