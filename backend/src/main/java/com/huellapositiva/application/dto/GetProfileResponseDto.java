package com.huellapositiva.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class GetProfileResponseDto {

    @Schema(
            description = "Name of volunteer",
            example = "Fernando Arnaldo"
    )
    private final String name;

    @Schema(
            description = "Surname of volunteer",
            example = "Santana Guajiro"
    )
    private final String surname;

    @Schema(
            description = "User's date of birth",
            example = "1990-06-27"
    )
    private final String birthDate;

    @Schema(
            description = "User's telephone. TIP: The international prefix can be +X, +XX, +XXX",
            example = "+850 12419287524"
    )
    private final String phoneNumber;

    @Schema(
            description = "User's email.",
            example = "fersanguajiro@huellapositiva.com"
    )
    private final String email;

    @Schema(
            description = "Where the user lives",
            example = "Las Palmas"
    )
    private final String province;

    @Schema(
            description = "Where the user lives. TIP: Can be +35xxx or 38xxx",
            example = "35241"
    )
    private final String zipCode;

    @Schema(
            description = "Where the user lives",
            example = "Agaete"
    )
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
    private final String island;

    @Schema(
            description = "User's photo",
            example = "https://s3.console.aws.amazon.com/s3/object/huellapositiva-dev/profile/photo?region=eu-west-1&prefix=00000000-0000-0000-0000-000000000000.png"
    )
    private final String photo;

    @Schema(
            description = "User's Curriculum Vitae",
            example = "https://s3.console.aws.amazon.com/s3/object/huellapositiva-dev/profile/cv?region=eu-west-1&prefix=00000000-0000-0000-0000-000000000000.pdf"
    )
    private final String curriculumVitae;

    @Schema(
            description = "User's URL twitter",
            example = "https://twitter.com/foo-bar"
    )
    private final String twitter;

    @Schema(
            description = "User's URL instagram",
            example = "https://instagram.com/foo-bar"
    )
    private final String instagram;

    @Schema(
            description = "User's URL linkedin",
            example = "https://linkedin.com/in/foo-bar"
    )
    private final String linkedin;

    @Schema(
            description = "User's extra information",
            example = "I'm a happy volunteer!!"
    )
    private final String additionalInformation;
}
