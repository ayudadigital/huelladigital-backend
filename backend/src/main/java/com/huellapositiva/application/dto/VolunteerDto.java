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
            example = "sdg87s6723kjb23487sdf"
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
