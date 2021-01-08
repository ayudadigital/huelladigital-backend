package com.huellapositiva.application.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.huellapositiva.application.dto.jackson.DtoProfileDeserializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@JsonDeserialize(using = DtoProfileDeserializer.class)
public class ProfileDto {

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z ]+$")
    private final String name;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z ]+$")
    private final String surname;

    @NotNull
    private final LocalDate birthDate;

    @NotBlank
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
    @Pattern(regexp = "^[a-zA-Z ]*$")
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
