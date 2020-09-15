package com.huellapositiva.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
@AllArgsConstructor
public class ProposalLiteDto {

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
    private final int minimumAge;

    @NotEmpty
    private final int maximumAge;

    @NotEmpty
    private final String closingProposalDate;

    @NotEmpty
    private final String startingVolunteeringDate;

    private final boolean published;

    @NotEmpty
    private final String description;

    @NotEmpty
    private final String duration;

    private final String imageURL;

}
