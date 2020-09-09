package com.huellapositiva.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ProposalResponseDto {

    private final String id;

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
    private final String startingProposalDate;

    @NotEmpty
    private final String closingProposalDate;

    @NotEmpty
    private final String startingVolunteeringDate;

    private final boolean published;

    @NotEmpty
    private final String description;

    @NotEmpty
    private final String instructions;

    @NotEmpty
    private final String extraInfo;

    @NotEmpty
    private final Integer durationInDays;

    @NotEmpty
    private final String category;

    private final String imageURL;

    private final List<VolunteerDto> inscribedVolunteers;

    private final Integer inscribedVolunteersCount;

    private final List<SkillDto> skills;

    private final List<String> requirements;
}
