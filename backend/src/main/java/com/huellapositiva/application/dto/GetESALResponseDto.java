package com.huellapositiva.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
public class GetESALResponseDto {

    @Schema(
            description = "Name of ESAL",
            example = "Canarias para todos"
    )
    @NotBlank
    private final String name;

    @Schema(
            description = "ESAL's description",
            example = "ESAL dedicada a ayudar en la recogida de alimentos y otros productos básicos."

    )
    @Size(max = 500)
    private final String description;

    @Schema(
            description = "ESAL's website",
            example = "https://www.foo-bar.com/"
    )
    @NotEmpty
    @Size(max = 255)
    private final String website;

    @Schema(
            description = "Whether the ESAL is registered as an Volunteering Entity by Gobierno de Canarias",
            example = "true"
    )
    private final boolean registeredEntity;

    @Schema(
            description = "Entity type of the ESAL",
            example = "ASSOCIATION"
    )
    @NotBlank
    private final String entityType;

    @Schema(
            description = "Where the ESAL is located. TIP: Only can be the eight islands of Canary Islands",
            example = "Tenerife"
    )
    @NotBlank
    private final String island;

    @Schema(
            description = "Where the ESAL is located. TIP: Can be +35xxx or +38xxx",
            example = "35041"
    )
    @NotBlank
    private final String zipCode;

    @Schema(
            description = "Where the ESAL is located",
            example = "Las Palmas"
    )
    private final String province;

    @Schema(
            description = "Where the ESAL is located",
            example = "Agaete"
    )
    private final String town;

    @Schema(
            description = "Where the ESAL is located",
            example = "Calle Guacimeta N3"
    )
    private final String address;
}
