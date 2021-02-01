package com.huellapositiva.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
public class AuthenticationRequestDto {


    @Schema(
            description = "Volunteer email unique",
            example = "jhon.doe@huellapositiva.com"
    )
    @NotNull
    @Email
    private final String email;

    @Schema(
            description = "Volunteer password",
            example = "albaricoque28"
    )
    @NotEmpty
    private final String password;
}
