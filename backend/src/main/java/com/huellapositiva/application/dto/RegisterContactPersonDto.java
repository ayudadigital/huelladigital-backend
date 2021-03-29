package com.huellapositiva.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.*;

@Data
@Builder
@AllArgsConstructor
public class RegisterContactPersonDto {

    @Schema(
            description = "Name of Contact Person",
            example = "Guajiro Ermenegildo"
    )
    @NotBlank
    @Pattern(regexp = "^[a-zA-ZáéíóúüñÁÉÍÓÚÜÑ ]+$")
    private final String name;

    @Schema(
            description = "Surname of volunteer",
            example = "Sánchez Inmaculado"
    )
    @NotBlank
    @Pattern(regexp = "^[a-zA-ZáéíóúüñÁÉÍÓÚÜÑ ]+$")
    private final String surname;

    @Schema(
            description = "User's telephone. TIP: The international preffix can be +X, +XX, +XXX",
            example = "+850 12419287524"
    )
    @NotBlank
    private final String phoneNumber;

    @Schema(
            description = "Organization email unique",
            example = "huelladigital@soymamut.com"
    )
    @NotBlank
    @Email
    private final String email;

    @Schema(
            description = "Organization password",
            example = ")OAx7H6$d'2]5F5R,}i%"
    )
    @NotBlank(message = "The password is not blank space")
    @Size(min = 6, message = "The password is too short")
    @Pattern(regexp = "^[a-zA-Z0-9.,:+`%!@#$^'?(){}~_/\\-\\[\\]]*$", message = "The new password does not match with the regular expresion")
    private final String password;
}
