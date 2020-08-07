package com.huellapositiva.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
@AllArgsConstructor
public class ProposalRequestDto {

    @NotEmpty
    private final String title;

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

    private boolean published;

    /** This operation will be implemented in the entity Proposal
     * @see com.huellapositiva.domain.model.entities.Proposal
     * @deprecated
     *  since = "Refactor to Domain Driven Design"
     *  forRemoval = true
     * */
    @Deprecated(since = "Refactor to Domain Driven Design", forRemoval = true)
    void publish() {
        throw new UnsupportedOperationException();
    }
}
