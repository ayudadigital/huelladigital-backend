package com.huellapositiva.domain.actions;

import com.huellapositiva.domain.dto.ChangeStatusToPublishedResult;
import com.huellapositiva.domain.model.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.domain.service.ProposalService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChangeReviewPendingProposalToPublishedAction {

    private final ProposalService proposalService;

    private final EmailCommunicationService emailCommunicationService;

    public void execute(String idProposal) {
        ChangeStatusToPublishedResult result = proposalService.changeStatusToPublished(idProposal);
        emailCommunicationService.sendMessageProposalPublished(result.getEmail(), result.getProposalTitle());
    }
}
