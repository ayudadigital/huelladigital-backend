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

    private String organizationName;

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

    /*Es raro que vayamos a la variable publish, así que mejor hacerlo método*/
    void publish() {

    }
}
