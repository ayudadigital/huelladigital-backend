package com.huellapositiva.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@Builder
@AllArgsConstructor
public class ESALRequestDto {

    @Schema(
            description = "Name of ESAL",
            example = "Canarias para todos"
    )
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z ]+$")
    private final String name;

    @Schema(
            description = "ESAL's description",
            example = "ESAL dedicada a ayudar en la recogida de alimentos."

    )
    private final String description;

    @Schema(
            description = "ESAL's website",
            example = "https://www.foo-bar.com/"
    )
    @Pattern(regexp = "^https?://(www.)?[-a-zA-Z0-9+&@#%=~_|]*.(com|es|net|org)/[-a-zA-Z0-9+&@#%=~_|]*")
    private final String website;

    @Schema(
            description = "Whether the ESAL is registered as an Volunteering Entity by Gobierno de Canarias",
            example = "true"
    )
    @NotBlank
    private final boolean registeredEntity;

    @Schema(
            description = "Entity type of the ESAL",
            example = "Asociacion"
    )
    @NotBlank
    @Pattern(regexp = "^(Asociacion|Fundacion|Federacion Deportiva|Colegio Profesional|Club Deportivo)$")
    private final String entityType;

    @Schema(
            description = "Whether the ESAL has accepted the Privacy Policy",
            example = "true"
    )
    @NotBlank
    private final boolean privacyPolicy;

    @Schema(
            description = "Whether the ESAL has accepted the Data Protection Policy",
            example = "true"
    )
    @NotBlank
    private final boolean dataProtectionPolicy;

    @Schema(
            description = "Where the ESAL is located. TIP: Only can be the eight islands of Canary Islands",
            example = "Gran Canaria"
    )
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z ]*$")
    private final String island;

    @Schema(
            description = "Where the ESAL is located. TIP: Can be +35xxx or 38xxx",
            example = "35241"
    )
    @NotBlank
    @Pattern(regexp = "^\\d{5}$")
    private final String zipCode;
}
