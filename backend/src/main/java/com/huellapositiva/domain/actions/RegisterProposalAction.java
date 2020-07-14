package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.ProposalRequestDto;
import com.huellapositiva.domain.service.ProposalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RegisterProposalAction {

    private final ProposalService proposalService;

    public void execute(ProposalRequestDto dto) {
        proposalService.registerProposal(dto);
    }
}
