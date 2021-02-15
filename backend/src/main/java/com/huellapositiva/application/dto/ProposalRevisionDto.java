package com.huellapositiva.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ProposalRevisionDto {

    @Schema(
            description = "If hasFeedback is false, the feedback will be empty",
            example = "La descripción tiene una deficiencia porque no se que está indicando."
    )
    private final String feedback;
}
