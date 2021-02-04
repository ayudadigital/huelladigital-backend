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

import static com.huellapositiva.domain.model.valueobjects.ProposalStatus.CHANGES_REQUESTED;
import static com.huellapositiva.domain.model.valueobjects.ProposalStatus.PUBLISHED;

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
        if (proposal.getStatus() != PUBLISHED) {
            throw new ProposalNotPublishedException("Proposal not found. Proposal ID: " + proposalId);
        }
        boolean isEnrollmentClosed = proposal.getClosingProposalDate().isBeforeNow();
        if (isEnrollmentClosed) {
            throw new ProposalEnrollmentClosedException();
        }
        proposal.inscribeVolunteer(volunteer);
        proposalRepository.save(proposal);
    }

    /**
     * Fetch the ESAL of the database for get the ContactPerson, change status of the proposal and send an email with the revision of the reviser.
     *
     * @param proposalId : The id of the proposal to be revised.
     * @param revisionDto : Contains the email reviser and the feedback if has it.
     * @param proposalURI : URI the proposal to revise.
     * @param accountId Account ID of logged user
     */
    public ProposalRevisionEmail proposalChangeStatusToChangedRequested(
            String proposalId,
            ProposalRevisionDto revisionDto,
            URI proposalURI,
            String accountId) {

        Proposal proposal = proposalRepository.fetch(proposalId);

        if (ProposalStatus.REVIEW_PENDING.getId() != proposal.getStatus().getId()) {
            throw new InvalidProposalStatusException();
        }

        ESAL esal = proposalRepository.fetch(proposalId).getEsal();
        ContactPerson contactPerson = contactPersonRepository.findByJoinedEsalId(esal.getId().toString());
        Reviser reviser = Reviser.from(credentialsRepository.findReviserByAccountId(accountId));

        ProposalRevisionEmail revisionBuilder = ProposalRevisionEmail.builder()
                .proposalId(new Id(proposalId))
                .proposalURI(proposalURI)
                .feedback(revisionDto.getFeedback())
                .esalContactPerson(contactPerson)
                .reviser(reviser)
                .token(Token.createToken())
                .build();

        Boolean hasFeedback =  revisionDto.getHasFeedback();
        if (hasFeedback != null && hasFeedback && revisionDto.getFeedback() == null) {
            revisionBuilder.setHasFeedback(false);
        } else {
            revisionBuilder.setHasFeedback(hasFeedback);
        }

        JpaProposalStatus jpaProposalStatus = jpaProposalStatusRepository.findByName(CHANGES_REQUESTED.toString().toLowerCase())
                .orElseThrow(() -> new StatusNotFoundException("Proposal status not found: " + proposalId));
        jpaProposalRepository.updateStatusById(proposal.getId().getValue(), jpaProposalStatus);

        return revisionBuilder;
    }
}
