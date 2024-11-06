package uk.ac.ebi.gdp.intervene.commons.dto.filehandler;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties
public record DeleteDirResponseDTO(String code,
                                   String taskId) {
}
