package com.huellapositiva.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.*;

@Data
@SuperBuilder
public class RegisterContactPersonDto extends UpdateContactPersonProfileRequestDto{


    @Schema(
            description = "Organization password",
            example = ")OAx7H6$d'2]5F5R,}i%"
    )
    @NotBlank(message = "The password is not blank space")
    @Size(min = 6, message = "The password is too short")
    @Pattern(regexp = "^[a-zA-Z0-9.,:+`%!@#$^'?(){}~_/\\-\\[\\]]*$", message = "The new password does not match with the regular expresion")
    private final String password;

}
