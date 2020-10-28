package com.huellapositiva.application.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProposalRevisionDto {

    private final String feedback;

    private String reviserEmail;

    private Boolean hasFeedback;
}
