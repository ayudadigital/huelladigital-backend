package com.huellapositiva.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class UpdateVolunteerProfileRequestDto {
    @Schema(
            description = "Name of volunteer",
            example = "Fernando Arnaldo"
    )
    @NotBlank
    @Pattern(regexp = "^[a-zA-ZáéíóúüñÁÉÍÓÚÜÑ ]+$")
    private final String name;

    @Schema(
            description = "Surname of volunteer",
            example = "Santana Guajiro"
    )
    @NotBlank
    @Pattern(regexp = "^[a-zA-ZáéíóúüñÁÉÍÓÚÜÑ ]+$")
    private final String surname;

    @Schema(
            description = "User's date of birth",
            example = "1990-06-27"
    )
    @NotNull
    private final LocalDate birthDate;

    @Schema(
            description = "User's telephone. TIP: The international preffix can be +X, +XX, +XXX",
            example = "+850 12419287524"
    )
    @NotBlank
    private final String phoneNumber;

    @Schema(
            description = "User's email.",
            example = "fersanguajiro@huellapositiva.com"
    )
    @NotNull
    @Email
    private final String email;

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
    @Pattern(regexp = "^[a-zA-ZáéíóúüñÁÉÍÓÚÜÑ ]+$")
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

    @Schema(
            description = "User's URL twitter",
            example = "https://twitter.com/foo-bar"
    )
    @Pattern(regexp = "^https?://(www.)?twitter.com/[-a-zA-Z0-9+&@#%=~_|]+/?")
    private final String twitter;

    @Schema(
            description = "User's URL instagram",
            example = "https://instagram.com/foo-bar"
    )
    @Pattern(regexp = "^https?://(www.)?instagram.com/[-a-zA-Z0-9+&@#%=~_|]*/?")
    private final String instagram;

    @Schema(
            description = "User's URL linkedin",
            example = "https://linkedin.com/in/foo-bar"
    )
    @Pattern(regexp = "^https?://(www.)?linkedin.com/in/[-a-zA-Z0-9+&@#%=~_|]*/?")
    private final String linkedin;

    @Schema(
            description = "User's extra information",
            example = "I'm a happy volunteer!!"
    )
    private final String additionalInformation;
}
