package com.huellapositiva.domain.actions;

import com.huellapositiva.domain.service.ProposalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChangeEnrollmentClosedProposalToPublishedAction {

    private final ProposalService proposalService;

    /**
     * Method for update status proposal to published from enrollment closed.
     *
     * @param idProposal : The ID of the proposal.
     */
    public void execute(String idProposal) {
        proposalService.changeStatusToPublishedFromEnrollmentClosed(idProposal);
    }
}
