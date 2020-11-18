package com.huellapositiva.application.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class VolunteerDto {

    private final String id;

    private final String emailAddress;

}
