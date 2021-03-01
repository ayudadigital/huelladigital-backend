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
     * @param proposalId : The ID of the proposal.
     */
    public void executeAsReviser(String proposalId) {
        ChangeStatusToPublishedResult result = proposalService.changeStatusToPublished(proposalId);
        emailCommunicationService.sendMessageProposalPublished(result.getEmail(), result.getProposalTitle());
    }

    public void executeAsContactPerson(String proposalId) {
        proposalService.changeStatusToPublishedFromEnrollmentClosed(proposalId);
    }
}
