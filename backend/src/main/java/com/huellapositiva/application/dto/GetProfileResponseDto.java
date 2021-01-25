package com.huellapositiva.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.*;

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
            description = "User's telephone. TIP: The international preffix can be +X, +XX, +XXX",
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
            example = "/storage/volunteers/89sfa98asfas687as6f8as6f8a7s6f8as.png"
    )
    private final String photo;

    @Schema(
            description = "User's Curriculum Vitae",
            example = "/storage/volunteers/sdfyuhdhg8df58weiou3.pdf"
    )
    private final String curriculumVitae;

    @Schema(
            description = "User's URL twitter",
            example = "https://twitter.com/policia"
    )
    private final String twitter;

    @Schema(
            description = "User's URL instagram",
            example = "https://instagram.com/policia"
    )
    private final String instagram;

    @Schema(
            description = "User's URL linkedin",
            example = "https://linkedin.com/in/policia"
    )
    private final String linkedin;

    @Schema(
            description = "User's extra information",
            example = "I'm a happy volunteer!!"
    )
    private final String additionalInformation;
}
