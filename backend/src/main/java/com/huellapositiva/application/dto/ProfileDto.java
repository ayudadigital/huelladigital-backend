package com.huellapositiva.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@Builder
@AllArgsConstructor
public class ProfileDto {

    @NotBlank
    private final String name;
    @NotBlank
    private final String surname;
    @NotBlank
    private final String birthDate;
    @NotBlank
    @Pattern(regexp = "^\\+(?:[0-9] ?){6,14}[0-9]$")
    private final String phoneNumber;
    @NotNull
    @Email(regexp = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$")
    private final String email;
    private final String province;
    @NotBlank
    private final String zipCode;
    private final String town;
    private final String address;
    @NotBlank
    private final String island;
    private final String photo;
    private final String curriculumVitae;
    private final String twitter;
    private final String instagram;
    private final String linkedin;
    private final String additionalInformation;

}
