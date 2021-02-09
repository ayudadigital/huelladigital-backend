package com.huellapositiva.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.*;

@Data
@Builder
@AllArgsConstructor
public class RegisterESALMemberRequestDto {

    @Schema(
            description = "Name of Contact Person",
            example = "Guajiro Ermenegildo"
    )
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z ]+$")
    private final String name;

    @Schema(
            description = "Surname of volunteer",
            example = "Sánchez Inmaculado"
    )
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z ]+$")
    private final String surname;

    @Schema(
            description = "User's telephone. TIP: The international preffix can be +X, +XX, +XXX",
            example = "+850 12419287524"
    )
    @NotBlank
    private final String telefono;

    @Schema(
            description = "Organization email unique",
            example = "huelladigital@soymamut.com"
    )
    @NotBlank
    @Email
    private final String email;

    @Schema(
            description = "Organization password",
            example = "albaricoque29"
    )
    @NotBlank
    private final String password;
}
