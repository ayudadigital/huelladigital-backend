package com.huellapositiva.domain.actions;

import com.huellapositiva.domain.dto.ChangeStatusToPublishedResult;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.domain.service.ProposalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PublishProposalAction {

    private final ProposalService proposalService;

    private final EmailCommunicationService emailCommunicationService;

    /**
     * Method for update status proposal to published and send email to Proposal Contact Person.
     *
     * @param idProposal : The ID of the proposal.
     */
    public void execute(String idProposal) {
        ChangeStatusToPublishedResult result = proposalService.changeStatusToPublished(idProposal);
        emailCommunicationService.sendMessageProposalPublished(result.getEmail(), result.getProposalTitle());
    }
}
