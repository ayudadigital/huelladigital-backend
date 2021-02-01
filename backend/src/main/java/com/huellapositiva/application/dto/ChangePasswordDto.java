package com.huellapositiva.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
public class ChangePasswordDto {

    @Schema(
            example = "myNewPassword"
    )
    @NotEmpty(message = "You must to write something")
    @Size(min = 6, message = "The password is too short")
    @Pattern(regexp = "^[a-zA-Z0-9.,:+`%!@#$^'?(){}~_/\\-\\[\\]]*$", message = "The new password does not match with the regular expresion")
    private final String newPassword;

    @Schema(
            example = "myOldPassword"
    )
    @NotEmpty(message = "You must to write something")
    @NotBlank(message = "The password is not blank space")
    private final String oldPassword;
}
