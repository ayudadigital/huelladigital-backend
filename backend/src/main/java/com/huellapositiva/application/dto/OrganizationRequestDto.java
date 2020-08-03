package com.huellapositiva.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
@AllArgsConstructor
public class OrganizationRequestDto {

    @NotEmpty
    private final String name;
}
