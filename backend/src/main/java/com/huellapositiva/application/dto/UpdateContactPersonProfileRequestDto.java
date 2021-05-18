package com.huellapositiva.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@Builder
@AllArgsConstructor
public class UpdateContactPersonProfileRequestDto {
    @Schema(
            description = "Contact person's name",
            example = "Francisco Arnaldo"
    )
    @NotBlank
    @Pattern(regexp = "^[a-zA-ZáéíóúüñÁÉÍÓÚÜÑ ]+$")
    private final String name;

    @Schema(
            description = "Contact person's surname",
            example = "Santana Guajiro"
    )
    @NotBlank
    @Pattern(regexp = "^[a-zA-ZáéíóúüñÁÉÍÓÚÜÑ ]+$")
    private final String surname;


    @Schema(
            description = "Contact person's telephone number. TIP: The international preffix can be +X, +XX, +XXX",
            example = "+850 12419287524"
    )
    @NotBlank
    private final String phoneNumber;

    @Schema(
            description = "Contact person's email.",
            example = "fransanguajiro@huellapositiva.com"
    )
    @NotNull
    @Email
    private final String email;
}
