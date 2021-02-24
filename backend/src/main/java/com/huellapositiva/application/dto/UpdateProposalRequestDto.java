package com.huellapositiva.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class UpdateProposalRequestDto {

    @NotBlank
    private final String id;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9 ]+$")
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
    @Pattern(regexp = "^[a-zA-Z0-9 ]*$")
    private final String requiredDays;

    @NotNull
    private final int minimumAge;

    @NotNull
    private final int maximumAge;

    @Schema(
            description = "Starting the enrollment of volunteers for the proposal, " +
                    "is before than closeProposalDate and startingVolunteeringDate",
            example = "1990-06-27"
    )
    private final LocalDate startingProposalDate;

    @Schema(
            description = "Close the enrollment of volunteers for the proposal, " +
                    "is before than startingVolunteeringDate",
            example = "1990-06-27"
    )
    @NotNull
    private final LocalDate closingProposalDate;

    @Schema(
            description = "Date the volunteering starts",
            example = "1990-06-27"
    )
    @NotNull
    private final LocalDate startingVolunteeringDate;

    @NotBlank
    private final String description;

    @NotBlank
    @Pattern(regexp = "^[0-9]*$")
    private final String durationInDays;

    @NotBlank
    private final String category;

    private final String[][] skills;

    private final String[] requirements;

    private final String extraInfo;

    private final String instructions;
}
