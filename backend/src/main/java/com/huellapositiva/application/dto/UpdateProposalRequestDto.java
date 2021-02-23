package com.huellapositiva.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
@Builder
@AllArgsConstructor
public class UpdateProposalRequestDto {

    @NotBlank
    private final String id;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z ]+$")
    private final String title;

    @Schema(
            description = "Where the user lives",
            example = "Las Palmas"
    )
    @Pattern(regexp = "^(Las Palmas|Santa Cruz de Tenerife)$")
    private final String province;

    @Schema(
            description = "Where the user lives. TIP: Can be +35xxx or 38xxx",
            example = "35241"
    )
    @NotBlank
    @Pattern(regexp = "^\\d{5}$")
    private final String zipCode;

    @Schema(
            description = "Where the user lives",
            example = "Agaete"
    )
    @Pattern(regexp = "^[a-zA-Z ]*$")
    private final String town;

    @Schema(
            description = "Where the user lives",
            example = "Calle Guacimeta N2"
    )
    private final String address;

    @Schema(
            description = "Where the user lives. TIP: Only can be the eight islands of Canary Islands",
            example = "Gran Canaria"
    )
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z ]*$")
    private final String island;

    @NotBlank
    private final String requiredDays;

    @NotBlank
    private final int minimumAge;

    @NotBlank
    private final int maximumAge;

    @NotBlank
    private final String startingProposalDate;

    @NotBlank
    private final String closingProposalDate;

    @NotBlank
    private final String startingVolunteeringDate;

    @NotBlank
    private final String description;

    @NotBlank
    private final String durationInDays;

    @NotBlank
    private final String category;

    @NotBlank
    private final String[][] skills;

    @NotBlank
    private final String[] requirements;

    @NotBlank
    private final String extraInfo;

    @NotBlank
    private final String instructions;
}
