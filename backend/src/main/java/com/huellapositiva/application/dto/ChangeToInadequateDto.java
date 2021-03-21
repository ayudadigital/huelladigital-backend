package com.huellapositiva.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

import javax.validation.constraints.Size;

@Value
public class ChangeToInadequateDto {@Size(max = 500)
    @Schema(
            description = "Reason for set proposal to inadequate",
            example = "Insufficient information provided."
    )
    String reason;
}
