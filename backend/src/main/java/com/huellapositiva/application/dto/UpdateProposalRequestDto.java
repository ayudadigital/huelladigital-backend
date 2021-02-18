package com.huellapositiva.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
@AllArgsConstructor
public class UpdateProposalRequestDto {

    @NotEmpty
    private final String id;

    @NotEmpty
    private final String title;

    @NotEmpty
    private final String province;

    @NotEmpty
    private final String town;

    @NotEmpty
    private final String address;

    @NotEmpty
    private final String zipCode;

    @NotEmpty
    private final String island;

    @NotEmpty
    private final String requiredDays;

    @NotEmpty
    private final int minimumAge;

    @NotEmpty
    private final int maximumAge;

    @NotEmpty
    private final String startingProposalDate;

    @NotEmpty
    private final String closingProposalDate;

    @NotEmpty
    private final String startingVolunteeringDate;

    @NotEmpty
    private final String description;

    @NotEmpty
    private final String durationInDays;

    @NotEmpty
    private final String category;

    @NotEmpty
    private final String[][] skills;

    @NotEmpty
    private final String[] requirements;

    @NotEmpty
    private final String extraInfo;

    @NotEmpty
    private final String instructions;
}
