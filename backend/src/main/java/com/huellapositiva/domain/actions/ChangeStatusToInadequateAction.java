package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.ChangeToInadequateDto;
import com.huellapositiva.domain.exception.InvalidProposalStatusException;
import com.huellapositiva.domain.model.entities.ContactPerson;
import com.huellapositiva.domain.model.entities.Proposal;
import com.huellapositiva.domain.repository.ContactPersonRepository;
import com.huellapositiva.domain.repository.ProposalRepository;
import com.huellapositiva.domain.service.EmailCommunicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.huellapositiva.domain.model.valueobjects.ProposalStatus.INADEQUATE;
import static com.huellapositiva.domain.model.valueobjects.ProposalStatus.REVIEW_PENDING;
import static java.lang.String.format;

@Service
@RequiredArgsConstructor
public class ChangeStatusToInadequateAction {

    @Autowired
    private ProposalRepository proposalRepository;

    @Autowired
    private EmailCommunicationService emailCommunicationService;

    @Autowired
    private ContactPersonRepository contactPersonRepository;

    public void execute(ChangeToInadequateDto dto, String proposalId) {
        Proposal proposal = proposalRepository.fetch(proposalId);
        if(proposal.getStatus() != REVIEW_PENDING) {
            throw new InvalidProposalStatusException(format("Invalid proposal transition status from %s to %s", proposal.getStatus(), INADEQUATE));
        }

        proposal.setStatus(INADEQUATE);
        proposalRepository.save(proposal);

        ContactPerson contactPerson = contactPersonRepository.findByJoinedEsalId(proposal.getEsal().getId().getValue());
        emailCommunicationService.sendInadequateProposalEmail(contactPerson.getEmailAddress(), proposal.getTitle(), dto.getReason());
    }
}
