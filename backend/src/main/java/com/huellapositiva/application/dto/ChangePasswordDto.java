package com.huellapositiva.application.dto;

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

    @NotEmpty
    @NotBlank
    @Size(min = 6)
    @Pattern(regexp = "^[a-zA-Z0-9àèìòùÀÈÌÒÙáéíóúýÁÉÍÓÚÝâêîôûÂÊÎÔÛãñõÃÑÕäëïöüÿÄËÏÖÜŸ.,:;+*/|%!@#$&€=<>() -]*$", message = "The new password does not match with the regular expresion")
    private final String newPassword;

    @NotEmpty
    @NotBlank
    @Size(min = 6)
    @Pattern(regexp = "^[a-zA-Z0-9àèìòùÀÈÌÒÙáéíóúýÁÉÍÓÚÝâêîôûÂÊÎÔÛãñõÃÑÕäëïöüÿÄËÏÖÜŸ.,:;+*/|%!@#$&€=<>() -]*$", message = "The old password does not match with the regular expresion")
    private final String oldPassword;
}
