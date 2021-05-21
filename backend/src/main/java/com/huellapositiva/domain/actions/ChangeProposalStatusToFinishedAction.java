package com.huellapositiva.domain.actions;

import com.huellapositiva.domain.service.ProposalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChangeProposalStatusToFinishedAction {

    private final ProposalService proposalService;

    /**
     * This method changes the status of the proposal to FINISHED
     *
     * @param proposalId Id of the proposal
     */
    public void execute(String proposalId){
        proposalService.changeStatusToFinished(proposalId);
    }
}
