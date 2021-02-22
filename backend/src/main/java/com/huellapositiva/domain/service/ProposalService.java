package com.huellapositiva.domain.service;

import com.huellapositiva.application.dto.ProposalRevisionDto;
import com.huellapositiva.application.exception.ProposalEnrollmentClosedException;
import com.huellapositiva.application.exception.ProposalNotPublishedException;
import com.huellapositiva.domain.exception.InvalidProposalStatusException;
import com.huellapositiva.domain.exception.StatusNotFoundException;
import com.huellapositiva.domain.model.entities.*;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.domain.model.valueobjects.ProposalRevisionEmail;
import com.huellapositiva.domain.model.valueobjects.ProposalStatus;
import com.huellapositiva.domain.model.valueobjects.Token;
import com.huellapositiva.domain.repository.ContactPersonRepository;
import com.huellapositiva.domain.repository.CredentialsRepository;
import com.huellapositiva.domain.repository.ProposalRepository;
import com.huellapositiva.infrastructure.orm.entities.JpaProposalStatus;
import com.huellapositiva.infrastructure.orm.repository.JpaProposalRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaProposalStatusRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;

import static com.huellapositiva.domain.model.valueobjects.ProposalStatus.*;

@Slf4j
@Service
@AllArgsConstructor
public class ProposalService {

    private final ProposalRepository proposalRepository;

    private final ContactPersonRepository contactPersonRepository;

    private final CredentialsRepository credentialsRepository;

    private final JpaProposalRepository jpaProposalRepository;

    private final JpaProposalStatusRepository jpaProposalStatusRepository;

    /**
     * This method fetches the proposal requested to enroll in and if enrollment is available it enrolls the volunteer
     *
     * @param proposalId proposal's id
     * @param volunteer volunteer information
     */
    public void enrollVolunteer(String proposalId, Volunteer volunteer) {
        Proposal proposal = proposalRepository.fetch(proposalId);

        if (proposal.getStatus() == ENROLLMENT_CLOSED) {
            throw new ProposalEnrollmentClosedException();
        }
        if (proposal.getStatus() != PUBLISHED) {
            throw new ProposalNotPublishedException("Proposal not found. Proposal ID: " + proposalId);
        }
        proposal.inscribeVolunteer(volunteer);
        proposalRepository.save(proposal);
    }

    /**
     * Fetch the ESAL of the database for get the ContactPerson, change status of the proposal and send an email with the revision of the reviser.
     *
     * @param proposalId : The id of the proposal to be revised.
     * @param proposalRevisionDto : Contains the email reviser and the feedback if has it.
     * @param proposalURI : URI the proposal to revise.
     * @param accountId Account ID of logged user
     */
    public ProposalRevisionEmail requestChanges(
            String proposalId,
            ProposalRevisionDto proposalRevisionDto,
            URI proposalURI,
            String accountId) {

        Proposal proposal = proposalRepository.fetch(proposalId);

        if (ProposalStatus.REVIEW_PENDING.getId() != proposal.getStatus().getId()) {
            throw new InvalidProposalStatusException();
        }

        ESAL esal = proposal.getEsal();
        ContactPerson contactPerson = contactPersonRepository.findByJoinedEsalId(esal.getId().toString());
        Reviser reviser = Reviser.from(credentialsRepository.findReviserByAccountId(accountId));

        ProposalRevisionEmail proposalRevisionEmail = ProposalRevisionEmail.builder()
                .proposalId(new Id(proposalId))
                .proposalURI(proposalURI)
                .feedback(proposalRevisionDto.getFeedback())
                .hasFeedback(hasFeedback(proposalRevisionDto))
                .esalContactPerson(contactPerson)
                .reviser(reviser)
                .token(Token.createToken())
                .build();

        JpaProposalStatus jpaProposalStatus = jpaProposalStatusRepository.findByName(CHANGES_REQUESTED.toString().toLowerCase())
                .orElseThrow(() -> new StatusNotFoundException("Proposal status not found: " + proposalId));
        jpaProposalRepository.updateStatusById(proposal.getId().getValue(), jpaProposalStatus);

        return proposalRevisionEmail;
    }

    /**
     * Check for feedback message
     *
     * @param proposalRevisionDto Contains the email reviser and the feedback if has it.
     */
    private boolean hasFeedback(ProposalRevisionDto proposalRevisionDto) {
        return proposalRevisionDto.getFeedback() != null;
    }
}
