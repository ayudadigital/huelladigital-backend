package com.huellapositiva.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
public class ChangePasswordDto {

    @NotEmpty
    @Size(min = 6, max = 15)
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "Nueva contrasenna falla")
    private final String newPassword;

    @NotEmpty
    @Size(min = 6, max = 15)
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "Vieja contrasenna falla")
    private final String oldPassword;
}
