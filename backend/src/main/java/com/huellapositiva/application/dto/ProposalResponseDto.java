package com.huellapositiva.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ProposalResponseDto {

    @Schema(
            example = "ff73034b-s78d-2331-asf8-afwe9fasf86e"
    )
    private final String id;

    @Schema(
            example = "Limpiando las plazas"
    )
    @NotEmpty
    private final String title;

    @Schema(
            example = "Colegas de la playa"
    )
    @NotEmpty
    private String esalName;

    @Schema(
            example = "Tenerife"
    )
    @NotEmpty
    private final String province;

    @Schema(
            example = "Orotava"
    )
    @NotEmpty
    private final String town;

    @Schema(
            example = "Calle Arriba, 23"
    )
    @NotEmpty
    private final String address;

    @Schema(
            example = "6"
    )
    @NotEmpty
    private final String requiredDays;

    @Schema(
            example = "26"
    )
    @NotEmpty
    private final int minimumAge;

    @Schema(
            example = "64"
    )
    @NotEmpty
    private final int maximumAge;

    @Schema(
            example = "2021-01-10"
    )
    @NotEmpty
    private final String startingProposalDate;

    @Schema(
            example = "2021-01-13"
    )
    @NotEmpty
    private final String closingProposalDate;

    @Schema(
            example = "2021-01-16"
    )
    @NotEmpty
    private final String startingVolunteeringDate;

    @Schema(
            example = "CLOSED"
    )
    private final int status;

    @Schema(
            example = "Unos compañeros para limpiar la playa"
    )
    @NotEmpty
    private final String description;

    @Schema(
            example = "Ropa comoda"
    )
    @NotEmpty
    private final String instructions;

    @Schema(
            example = "Hay salitre"
    )
    @NotEmpty
    private final String extraInfo;

    @Schema(
            example = "10"
    )
    @NotEmpty
    private final Integer durationInDays;

    @Schema(
            example = "ON_SITE"
    )
    @NotEmpty
    private final String category;

    @Schema(
            example = "https://s3.console.aws.amazon.com/s3/object/huellapositiva-dev/profile/photo?region=eu-west-1&prefix=00000000-0000-0000-0000-000000000000.png"
    )
    private final String imageURL;

    private final List<VolunteerDto> inscribedVolunteers;

    @Schema(
            example = "20"
    )
    private final Integer inscribedVolunteersCount;

    private final List<SkillDto> skills;

    @Schema(
            example = "Lleva tu tarjeta sanitaria"
    )
    private final List<String> requirements;
}
