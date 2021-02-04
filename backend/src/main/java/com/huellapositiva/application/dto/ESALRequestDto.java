package com.huellapositiva.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
@AllArgsConstructor
public class ESALRequestDto {

    @Schema(
            example = "Canarias para todos"
    )
    @NotEmpty
    private final String name;

    private final String description;
    @NotEmpty
    private final String logoUrl;

    private final String webpage;
    @NotEmpty
    private final Boolean registeredEntity;
    @NotEmpty
    private final String entityType;
    @NotEmpty
    private final Boolean privacyPolicy;
    @NotEmpty
    private final Boolean dataProtectionPolicy;
    @NotEmpty
    private final String island;
    @NotEmpty
    private final String zipCode;
}
