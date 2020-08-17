package com.huellapositiva.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
public class ProposalResponseDto {

    @NotEmpty
    private final String title;

    @NotEmpty
    private String esalName;

    @NotEmpty
    private final String province;

    @NotEmpty
    private final String town;

    @NotEmpty
    private final String address;

    @NotEmpty
    private final String requiredDays;

    @NotEmpty
    private final int minimumAge;

    @NotEmpty
    private final int maximumAge;

    @NotEmpty
    private final String expirationDate;

    private final boolean published;

    @NotEmpty
    private final String description;

    @NotEmpty
    private final Integer durationInDays;

    @NotEmpty
    private final String category;

    @NotEmpty
    private final Date startingDate;
}
