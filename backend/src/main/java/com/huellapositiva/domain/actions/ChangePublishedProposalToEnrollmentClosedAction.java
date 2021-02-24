package com.huellapositiva.domain.actions;

import com.huellapositiva.domain.service.ProposalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
public class ChangePublishedProposalToEnrollmentClosedAction {
    private final ProposalService proposalService;

    public void execute(String idProposal) {
        proposalService.changeStatusToEnrollmentClosed(idProposal);
    }
}
