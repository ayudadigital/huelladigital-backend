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
            example = "ff79038b-3fec-41f0-bab8-6e0d11db986e"
    )
    private final String id;

    @Schema(
            example = "Reforestando nuestra tierra"
    )
    @NotEmpty
    private final String title;

    @Schema(
            example = "Amigos de la naturaleza"
    )
    @NotEmpty
    private String esalName;

    @Schema(
            example = "Gran Canaria"
    )
    @NotEmpty
    private final String province;

    @Schema(
            example = "Arucas"
    )
    @NotEmpty
    private final String town;

    @Schema(
            example = "Calle Cerrillo, 47"
    )
    @NotEmpty
    private final String address;

    @Schema(
            example = "12"
    )
    @NotEmpty
    private final String requiredDays;

    @Schema(
            example = "16"
    )
    @NotEmpty
    private final int minimumAge;

    @Schema(
            example = "65"
    )
    @NotEmpty
    private final int maximumAge;

    @Schema(
            example = "2021-02-16"
    )
    @NotEmpty
    private final String startingProposalDate;

    @Schema(
            example = "2021-02-20"
    )
    @NotEmpty
    private final String closingProposalDate;

    @Schema(
            example = "2021-02-24"
    )
    @NotEmpty
    private final String startingVolunteeringDate;

    @Schema(
            example = "PUBLISHED"
    )
    private final int status;

    @Schema(
            example = "Un voluntariado muy bueno para reforestar Arucas"
    )
    @NotEmpty
    private final String description;

    @Schema(
            example = "Lleva ropa cómoda y botas de montaña"
    )
    @NotEmpty
    private final String instructions;

    @Schema(
            example = "No es recomendado para alérgicos"
    )
    @NotEmpty
    private final String extraInfo;

    @Schema(
            example = "30"
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
            example = "25"
    )
    private final Integer inscribedVolunteersCount;

    private final List<SkillDto> skills;

    @Schema(
            example = "Lleva tu DNI"
    )
    private final List<String> requirements;
}
