package com.huellapositiva.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
@AllArgsConstructor
public class ChangeStatusVolunteerDto {

    private String idProposal;
    private String idVolunteer;
    private boolean confirmed;
}
