package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.ProposalRevisionDto;
import com.huellapositiva.domain.model.entities.ESAL;
import com.huellapositiva.domain.model.valueobjects.ContactPerson;
import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.domain.model.valueobjects.ProposalRevisionEmail;
import com.huellapositiva.domain.repository.ContactPersonRepository;
import com.huellapositiva.domain.repository.ProposalRepository;
import com.huellapositiva.domain.service.EmailCommunicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;

@RequiredArgsConstructor
@Service
public class SubmitProposalRevisionAction {

    private final EmailCommunicationService communicationService;

    private final ProposalRepository proposalRepository;

    private final ContactPersonRepository contactPersonRepository;

    public void execute(String proposalId, ProposalRevisionDto overview, URI proposalURI) {
        ESAL esal = proposalRepository.fetch(proposalId).getEsal();
        ContactPerson contactPerson = contactPersonRepository.findByJoinedEsalId(esal.getId().toString());
        ProposalRevisionEmail revision = ProposalRevisionEmail.builder()
                .proposalId(new Id(proposalId))
                .proposalURI(proposalURI)
                .overview(overview.getRevisionOverview())
                .emailAddress(EmailAddress.from(contactPerson.getEmailAddress().toString()))
                .build();
        communicationService.sendSubmittedProposalRevision(revision);
    }
}
