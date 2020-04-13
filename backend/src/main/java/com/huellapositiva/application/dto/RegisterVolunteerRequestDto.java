package com.huellapositiva.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RegisterVolunteerRequestDto {

    private final String email;

    private final String password;
}
