package com.huellapositiva.domain.model.entities;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class Proposal {

    @NotEmpty
    private final String id;

    @NotEmpty
    private final String title;

    @NotEmpty
    private final ESAL esal;

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

    private boolean published;

    private final List<Volunteer> inscribedVolunteers = new ArrayList<>();

    public void inscribeVolunteer(Volunteer volunteer) {
        inscribedVolunteers.add(volunteer);
    }
}
