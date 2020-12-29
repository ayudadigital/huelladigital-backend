package com.huellapositiva.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ProfileDto {

    private final String name;
    private final String surname;
    private final String birthDate;
    private final String phoneNumber;
    private final String email;
    private final String province;
    private final String zipCode;
    private final String town;
    private final String address;
    private final String island;
    private final String photo;
    private final String curriculumVitae;
    private final String twitter;
    private final String instagram;
    private final String linkedin;
    private final String additionalInformation;

}
