package com.huellapositiva.domain.actions;

import com.huellapositiva.domain.service.ProposalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
public class CloseProposalEnrollmentAction {
    private final ProposalService proposalService;

    /**
     * Updates the proposal status from PUBLISHED to ENROLLMENT_CLOSED.
     * @param idProposal
     */
    public void execute(String idProposal) {
        proposalService.closeEnrollment(idProposal);
    }
}
