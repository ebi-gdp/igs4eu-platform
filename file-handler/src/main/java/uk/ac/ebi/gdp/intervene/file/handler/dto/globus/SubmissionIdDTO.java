package uk.ac.ebi.gdp.intervene.file.handler.dto.globus;

import com.fasterxml.jackson.annotation.JsonGetter;

public record SubmissionIdDTO(@JsonGetter("DATA_TYPE") String dataType,
                              @JsonGetter("value") String submissionId) {
}
