package com.huellapositiva.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ChangeStatusVolunteerDto {

    @Schema(
            example = "ff79038b-3fec-41f0-bab8-6e0d11db986e"
    )
    private String idProposal;

    @Schema(
            example = "ff79038b-3fec-41f0-bab8-6e0d11db986e"
    )
    private String idVolunteer;

    @Schema(
            example = "true"
    )
    private boolean confirmed;
}
