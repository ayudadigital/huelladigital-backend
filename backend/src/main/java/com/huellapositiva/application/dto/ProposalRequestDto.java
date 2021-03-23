package com.huellapositiva.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ProposalRequestDto {

    @Schema(
            description = "Title of the proposal",
            example = "Recogida de ropa"
    )
    @NotEmpty
    private final String title;

    @Schema(
            description = "Name of the esal owner of the proposal",
            example = "Canarias para todos"
    )
    @NotBlank
    private String esalName;

    @Schema(
            description = "Where the proposal will take place",
            example = "Las Palmas"
    )
    @Pattern(regexp = "^(Las Palmas|Santa Cruz de Tenerife)$")
    private final String province;

    @Schema(
            description = "Where the proposal will take place",
            example = "Agaete"
    )
    @Pattern(regexp = "^[a-zA-ZáéíóúüñÁÉÍÓÚÜÑ ]+$")
    private final String town;

    @Schema(
            description = "Where the proposal will take place",
            example = "Calle Guacimeta N2"
    )
    private final String address;

    @Schema(
            description = "Where the proposal will take place. TIP: Can be +35xxx or 38xxx",
            example = "35241"
    )
    @NotBlank
    @Pattern(regexp = "^\\d{5}$")
    private final String zipCode;

    @Schema(
            description = "Where the proposal will take place. TIP: Only can be the eight islands of Canary Islands",
            example = "Gran Canaria"
    )
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z ]*$")
    private final String island;

    @Schema(
            description = "When will the proposal take place",
            example = "Mañanas"
    )
    @NotBlank
    private final String requiredDays;

    @Schema(
            description = "Minimum age for a volunteer to join",
            example = "18"
    )
    @NotEmpty
    private final int minimumAge;

    @Schema(
            description = "Maximum age for a volunteer to join",
            example = "80"
    )
    @NotEmpty
    private final int maximumAge;

    @Schema(
            description = "Date when the proposal opens",
            example = "23-01-2020"
    )
    @NotBlank
    @Pattern(regexp = "^\\d{2}-\\d{2}-\\d{4}$")
    private final String startingProposalDate;

    @Schema(
            description = "Date when the proposal closes",
            example = "28-01-2020"
    )
    @NotBlank
    @Pattern(regexp = "^\\d{2}-\\d{2}-\\d{4}$")
    private final String closingProposalDate;

    @Schema(
            description = "Date when the volunteering starts",
            example = "30-01-2020"
    )
    @NotBlank
    @Pattern(regexp = "^\\d{2}-\\d{2}-\\d{4}$")
    private final String startingVolunteeringDate;

    @Schema(
            description = "The proposal's description",
            example = "Recogida de ropa en la laguna"
    )
    @NotBlank
    private final String description;

    @Schema(
            description = "The proposal's duration",
            example = "1 semana+"
    )
    @NotEmpty
    private final String durationInDays;

    @Schema(
            description = "The proposal's category",
            example = "ON_SITE"
    )
    @Pattern(regexp = "^(ON_SITE|REMOTE|MIXED)$")
    private final String category;

    @Schema(
            description = "The skills you will get from volunteering",
            example = "Habilidad, Descripción; Negociación, Saber regatear."
    )
    @NotEmpty
    private final List<SkillDto> skills;

    @Schema(
            description = "What the volunteer needs to perform the volunteering",
            example = "Forma física para cargar con la ropa, Disponibilidad horaria"
    )
    @NotEmpty
    private final List<String> requirements;

    @Schema(
            description = "The proposal's extra information",
            example = "Es recomendable tener ganas de recoger ropa"
    )
    private final String extraInfo;

    @Schema(
            description = "Instructions to join the proposal",
            example = "Se seleccionarán a los primeros 100 voluntarios"
    )
    private final String instructions;
}
