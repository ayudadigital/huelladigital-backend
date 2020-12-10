package com.huellapositiva.domain.model.valueobjects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ProfileVolunteer {
    private final String twitter;
    private final String instagram;
    private final String linkedin;
    private final String photoUrl;
    private final String curriculumUrl;
    private final String additionalInformation;
}
