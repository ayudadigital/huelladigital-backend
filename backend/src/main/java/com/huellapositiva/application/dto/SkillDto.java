package com.huellapositiva.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SkillDto {

    @Schema(
            example = "Alta resistencia"
    )
    private final String name;

    @Schema(
            example = "Posiblemente movamos macetas en peso muerto"
    )
    private final String description;

}
