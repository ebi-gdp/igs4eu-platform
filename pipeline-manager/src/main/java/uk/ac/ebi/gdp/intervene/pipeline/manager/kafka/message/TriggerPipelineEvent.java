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
package uk.ac.ebi.gdp.intervene.pipeline.manager.kafka.message;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TriggerPipelineEvent {

    @JsonProperty("target_genomes")
    private List<TargetGenomes> targetGenomes;

    @JsonProperty("nxf_params_file")
    private NXFParamsFile nxfParamsFile;

    @JsonProperty("nxf_work")
    private String nxfWork;
    private String id;

    public TriggerPipelineEvent(final List<TargetGenomes> targetGenomes,
                                final NXFParamsFile nxfParamsFile,
                                final String nxfWork,
                                final String id) {
        this.targetGenomes = targetGenomes;
        this.nxfParamsFile = nxfParamsFile;
        this.nxfWork = nxfWork;
        this.id = id;
    }

    public List<TargetGenomes> getTargetGenomes() {
        return targetGenomes;
    }

    public void setTargetGenomes(List<TargetGenomes> targetGenomes) {
        this.targetGenomes = targetGenomes;
    }

    public NXFParamsFile getNxfParamsFile() {
        return nxfParamsFile;
    }

    public void setNxfParamsFile(NXFParamsFile nxfParamsFile) {
        this.nxfParamsFile = nxfParamsFile;
    }

    public String getNxfWork() {
        return nxfWork;
    }

    public void setNxfWork(String nxfWork) {
        this.nxfWork = nxfWork;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public static class TargetGenomes {
        private String sample;

        @JsonProperty("vcf_path")
        private String vcfPath;
        private int chrom;

        public TargetGenomes(final String sample,
                             final String vcfPath,
                             final int chrom) {
            this.sample = sample;
            this.vcfPath = vcfPath;
            this.chrom = chrom;
        }

        public String getSample() {
            return sample;
        }

        public void setSample(String sample) {
            this.sample = sample;
        }

        public String getVcfPath() {
            return vcfPath;
        }

        public void setVcfPath(String vcfPath) {
            this.vcfPath = vcfPath;
        }

        public int getChrom() {
            return chrom;
        }

        public void setChrom(int chrom) {
            this.chrom = chrom;
        }
    }

    public static class NXFParamsFile {
        @JsonProperty("scorefile")
        private String scoreFile;

        @JsonProperty("min_over_lap")
        private float minOverlap;

        @JsonProperty("max_memory")
        private String maxMemory;

        @JsonProperty("format")
        private FormatType formatType;
        private String outdir;

        public NXFParamsFile(final String scoreFile,
                             final float minOverlap,
                             final String maxMemory,
                             final FormatType formatType,
                             final String outdir) {
            this.scoreFile = scoreFile;
            this.minOverlap = minOverlap;
            this.maxMemory = maxMemory;
            this.formatType = formatType;
            this.outdir = outdir;
        }

        public String getScoreFile() {
            return scoreFile;
        }

        public void setScoreFile(String scoreFile) {
            this.scoreFile = scoreFile;
        }

        public float getMinOverlap() {
            return minOverlap;
        }

        public void setMinOverlap(float minOverlap) {
            this.minOverlap = minOverlap;
        }

        public String getMaxMemory() {
            return maxMemory;
        }

        public void setMaxMemory(String maxMemory) {
            this.maxMemory = maxMemory;
        }

        public String getFormatType() {
            return formatType.getFormatType();
        }

        public void setFormatType(FormatType formatType) {
            this.formatType = formatType;
        }

        public String getOutdir() {
            return outdir;
        }

        public void setOutdir(String outdir) {
            this.outdir = outdir;
        }
    }

    public enum FormatType {
        JSON("json");

        private final String formatType;

        FormatType(final String formatType) {
            this.formatType = formatType;
        }

        public String getFormatType() {
            return formatType;
        }
    }
}
