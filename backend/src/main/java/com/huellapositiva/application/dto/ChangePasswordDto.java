package com.huellapositiva.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
@AllArgsConstructor
public class ChangePasswordDto {

    @NotEmpty
    private final String newPassword;

    @NotEmpty
    private final String oldPassword;
}
