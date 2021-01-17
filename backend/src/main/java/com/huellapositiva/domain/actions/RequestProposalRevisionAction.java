package com.huellapositiva.domain.actions;

import com.huellapositiva.domain.model.valueobjects.ProposalRevisionRequestEmail;
import com.huellapositiva.domain.service.EmailCommunicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;

@Service
@RequiredArgsConstructor
@Transactional
public class RequestProposalRevisionAction {

    private final EmailCommunicationService communicationService;

    @Value("${huellapositiva.revision.email.from}")
    private String reviserEmail;

    /**
     * This method asks for a reviser to revise a proposal from the given URI. It sends him a notification through email.
     *
     * @param proposalUri URI of proposal
     */
    public void execute(URI proposalUri) {
        ProposalRevisionRequestEmail email = ProposalRevisionRequestEmail.from(reviserEmail, proposalUri.toString());
        communicationService.sendRevisionRequestEmail(email);
    }
}
