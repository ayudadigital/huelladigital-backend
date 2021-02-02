package com.huellapositiva.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class VolunteerDto {

    @Schema(
            example = "ff79038b-3fec-41f0-bab8-6e0d11db986e"
    )
    private final String id;

    @Schema(
            example = "foo@huellapositiva.com"
    )
    private final String emailAddress;

    @Schema(
            example = "True"
    )
    private final Boolean confirmed;

}
