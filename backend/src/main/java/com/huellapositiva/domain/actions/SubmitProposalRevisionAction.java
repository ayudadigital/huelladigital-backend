package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.ProposalRevisionDto;
import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.domain.model.valueobjects.ProposalRevisionEmail;
import com.huellapositiva.domain.repository.ESALRepository;
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

    public void execute(String proposalId, ProposalRevisionDto overview, URI proposalURI) {
        ProposalRevisionEmail revision = ProposalRevisionEmail.builder()
                .proposalId(new Id(proposalId))
                .proposalURI(proposalURI)
                .overview(overview.getRevisionOverview())
                .emailAddress(EmailAddress.from(proposalRepository.fetch(proposalId).getEsal().getEmail().toString()))
                .build();
        communicationService.sendSubmittedProposalRevision(revision);
    }
}
