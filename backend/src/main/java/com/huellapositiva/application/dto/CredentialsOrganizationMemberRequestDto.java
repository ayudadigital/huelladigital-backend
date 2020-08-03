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
public class CredentialsOrganizationMemberRequestDto {

    @Schema(
            description = "Organization email unique",
            example = "huelladigital@soymamut.com"
    )
    @NotNull
    @Email(regexp = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$")
    private final String email;

    @Schema(
            description = "Organization password",
            example = "albaricoque29"
    )
    @NotEmpty
    private final String password;
}
