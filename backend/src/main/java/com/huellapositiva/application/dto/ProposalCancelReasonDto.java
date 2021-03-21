package com.huellapositiva.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
public class ProposalCancelReasonDto {
    @Size(max = 500)
    @Schema(
            description = "Reason for cancelling proposal",
            example = "Not enough volunteers listed"
    )
    String reason;
}
