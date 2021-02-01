package com.huellapositiva.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
@AllArgsConstructor
public class ProposalLiteDto {

    @Schema(
            example = "ff79038b-3fec-41f0-bab8-6e0d11db986e"
    )
    private final String id;

    @Schema(
            example = "Recogida de ropa"
    )
    @NotEmpty
    private final String title;

    @Schema(
            example = "PUBLISHED"
    )
    private final String status;

    @Schema(
            example = "Las Palmas"
    )
    @NotEmpty
    private final String province;

    @Schema(
            example = "Arucas"
    )
    @NotEmpty
    private final String town;

    @Schema(
            example = "Calle Murillo, 25"
    )
    @NotEmpty
    private final String address;

    @Schema(
            example = "20"
    )
    @NotEmpty
    private final int minimumAge;

    @Schema(
            example = "65"
    )
    @NotEmpty
    private final int maximumAge;

    @Schema(
            example = "2021-03-21"
    )
    @NotEmpty
    private final String closingProposalDate;

    @Schema(
            example = "2021-03-25"
    )
    @NotEmpty
    private final String startingVolunteeringDate;

    @Schema(
            example = "Una propuesta de voluntariado para recoger ropa"
    )
    @NotEmpty
    private final String description;

    @Schema(
            example = "25"
    )
    @NotEmpty
    private final String duration;

    @Schema(
            example = "https://s3.console.aws.amazon.com/s3/object/huellapositiva-dev/profile/photo?region=eu-west-1&prefix=00000000-0000-0000-0000-000000000000.png"
    )
    private final String imageURL;

}
