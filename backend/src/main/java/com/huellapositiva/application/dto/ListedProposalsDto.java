package com.huellapositiva.application.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class ListedProposalsDto {

    private final List<ProposalLiteDto> proposals;

}
