package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.ProposalRevisionDto;
import com.huellapositiva.domain.model.entities.ContactPerson;
import com.huellapositiva.domain.model.entities.ESAL;
import com.huellapositiva.domain.model.entities.Reviser;
import com.huellapositiva.domain.model.valueobjects.*;
import com.huellapositiva.domain.repository.ContactPersonRepository;
import com.huellapositiva.domain.repository.CredentialsRepository;
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

    private final CredentialsRepository credentialsRepository;

    /**
     * Fecht the ESAL of the database for get the ContacPerson and send an email with the revision of the revisor.
     *
     * @param proposalId : The identificator of the proposal to be revised.
     * @param revisionDto : Contains the email reviser and the feedback if has it.
     * @param proposalURI : URI the proposal to revise.
     */

    public void execute(String proposalId, ProposalRevisionDto revisionDto, URI proposalURI) {
        ESAL esal = proposalRepository.fetch(proposalId).getEsal();
        ContactPerson contactPerson = contactPersonRepository.findByJoinedEsalId(esal.getId().toString());
        Reviser reviser = Reviser.from(credentialsRepository.findByEmail(revisionDto.getReviserEmail()));
        Boolean hasFeedback =  revisionDto.getHasFeedback();
        if (hasFeedback != null && hasFeedback && revisionDto.getFeedback() == null) {
            revisionDto.setHasFeedback(false);
        }
        ProposalRevisionEmail revision = ProposalRevisionEmail.builder()
                .proposalId(new Id(proposalId))
                .proposalURI(proposalURI)
                .feedback(revisionDto.getFeedback())
                .hasFeedback(revisionDto.getHasFeedback())
                .esalContactPerson(contactPerson)
                .reviser(reviser)
                .build();
        communicationService.sendSubmittedProposalRevision(revision);
    }
}
