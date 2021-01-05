package com.huellapositiva.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.*;

@Data
@Builder
@AllArgsConstructor
public class ProfileDto {

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z ]+$")
    private final String name;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z ]+$")
    private final String surname;

    @NotBlank
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}")
    private final String birthDate;

    @NotBlank
    @Pattern(regexp = "^\\+(?:[0-9] ?){6,14}[0-9]$")
    private final String phoneNumber;

    @NotNull
    @Email(regexp = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$")
    private final String email;

    @Pattern(regexp = "^[Las Palmas|Santa Cruz]*$")
    private final String province;

    @NotBlank
    @Pattern(regexp = "^\\d{5}$")
    private final String zipCode;

    @Pattern(regexp = "^[a-zA-Z ]*$")
    private final String town;

    private final String address;

    @NotBlank
    @Pattern(regexp = "^(El Hierro|Fuerteventura|Gran Canaria|Lanzarote|La Gomera|La Graciosa|La Palma|Tenerife)$")
    private final String island;

    @Null
    private final String photo;

    @Null
    private final String curriculumVitae;

    @Pattern(regexp = "^https?://(www.)?twitter.com/[-a-zA-Z0-9+&@#%=~_|]+")
    private final String twitter;

    @Pattern(regexp = "^https?://(www.)?instagram.com/[-a-zA-Z0-9+&@#%=~_|]*")
    private final String instagram;

    @Pattern(regexp = "^https?://(www.)?linkedin.com/in/[-a-zA-Z0-9+&@#%=~_|]*")
    private final String linkedin;

    private final String additionalInformation;
}
