package com.huellapositiva.domain.service;

import com.huellapositiva.application.dto.ProposalRevisionDto;
import com.huellapositiva.application.dto.UpdateProposalRequestDto;
import com.huellapositiva.application.exception.*;
import com.huellapositiva.domain.dto.ChangeStatusToPublishedResult;
import com.huellapositiva.domain.exception.InvalidProposalStatusException;
import com.huellapositiva.domain.exception.StatusNotFoundException;
import com.huellapositiva.domain.model.entities.*;
import com.huellapositiva.domain.model.valueobjects.*;
import com.huellapositiva.domain.repository.ContactPersonRepository;
import com.huellapositiva.domain.repository.CredentialsRepository;
import com.huellapositiva.domain.repository.ProposalRepository;
import com.huellapositiva.infrastructure.orm.entities.JpaContactPerson;
import com.huellapositiva.infrastructure.orm.entities.JpaCredential;
import com.huellapositiva.infrastructure.orm.entities.JpaProposal;
import com.huellapositiva.infrastructure.orm.entities.JpaProposalStatus;
import com.huellapositiva.infrastructure.orm.repository.JpaContactPersonRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaCredentialRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaProposalRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaProposalStatusRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.net.URI;
import java.text.ParseException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.huellapositiva.domain.model.valueobjects.ProposalStatus.*;
import static java.lang.String.format;
import static java.util.Collections.emptyList;

@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class ProposalService {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyy");

    private final ProposalRepository proposalRepository;

    private final ContactPersonRepository contactPersonRepository;

    private final CredentialsRepository credentialsRepository;

    private final JpaProposalRepository jpaProposalRepository;

    private final JpaProposalStatusRepository jpaProposalStatusRepository;

    private final JpaCredentialRepository jpaCredentialRepository;

    private final JpaContactPersonRepository jpaContactPersonRepository;

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
        proposalRepository.insert(proposal);
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
            throw new InvalidProposalStatusException(format("Invalid proposal transition status from %s to %s", REVIEW_PENDING, CHANGES_REQUESTED));
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
     * This method updates the proposal and changes its status by REVIEW_PENDING
     *
     * @param proposalId Id of the proposal
     * @param updateProposalRequestDto The new information about the proposal
     * @param accountId The account id of the user
     */
    public void updateProposal(String proposalId, UpdateProposalRequestDto updateProposalRequestDto, String accountId) throws ParseException {
        Proposal proposal = proposalRepository.fetch(proposalId);
        JpaCredential jpaCredential = jpaCredentialRepository.findByAccountId(accountId)
                .orElseThrow(() -> new UserNotFoundException("Contact person not found. Account ID: " + accountId));
        validationsOfUpdateProposal(updateProposalRequestDto, jpaCredential, proposal.getEsal().getContactPersonEmail().toString());

        if (FINISHED.toString().equals(proposal.getStatus().toString()) ||
                CANCELLED.toString().equals(proposal.getStatus().toString()) ||
                INADEQUATE.toString().equals(proposal.getStatus().toString())) {
            throw new InvalidProposalStatusException("Cannot update proposal with status " + proposal.getStatus());
        }

        proposal.setStatus(REVIEW_PENDING);

        proposal.setTitle(updateProposalRequestDto.getTitle());
        proposal.getLocation().setProvince(updateProposalRequestDto.getProvince());
        proposal.getLocation().setIsland(updateProposalRequestDto.getIsland());
        proposal.getLocation().setTown(updateProposalRequestDto.getTown());
        proposal.getLocation().setZipCode(updateProposalRequestDto.getZipCode());
        proposal.getLocation().setAddress(updateProposalRequestDto.getAddress());
        proposal.setRequiredDays(updateProposalRequestDto.getRequiredDays());
        proposal.setPermittedAgeRange(
                AgeRange.create(
                        updateProposalRequestDto.getMinimumAge(),
                        updateProposalRequestDto.getMaximumAge()
                )
        );

        if (updateProposalRequestDto.getStartingProposalDate() != null){
            proposal.setStartingProposalDate(
                    ProposalDate.createStartingProposalDate(updateProposalRequestDto.getStartingProposalDate().format(formatter))
            );
        }
        proposal.setClosingProposalDate(
                ProposalDate.createClosingProposalDate(updateProposalRequestDto.getClosingProposalDate().format(formatter))
        );
        proposal.setStartingVolunteeringDate(
                ProposalDate.createStartingVolunteeringDate(updateProposalRequestDto.getStartingVolunteeringDate().format(formatter))
        );

        proposal.setDescription(updateProposalRequestDto.getDescription());
        proposal.setDurationInDays(updateProposalRequestDto.getDurationInDays());
        proposal.setCategory(ProposalCategory.getCategory(updateProposalRequestDto.getCategory()));
        proposal.setExtraInfo(updateProposalRequestDto.getExtraInfo());
        proposal.setInstructions(updateProposalRequestDto.getInstructions());

        addNewSkills(updateProposalRequestDto, proposal);
        addNewRequeriments(updateProposalRequestDto, proposal);

        proposalRepository.update(proposal);
    }

    /**
     * Some validations of the proposal fields
     *
     * @param updateProposalRequestDto The new information about the proposal
     * @param jpaCredential The credentials of the Contact Person
     * @param email The email of the Contact Person for to check if the proposal has the same email
     */
    private void validationsOfUpdateProposal(UpdateProposalRequestDto updateProposalRequestDto, JpaCredential jpaCredential, String email) {
        if(!(jpaCredential.getEmail().equals(email))) {
            throw new ProposalNotLinkedWithContactPersonException("This proposal not linked with your account");
        }
        if(Location.isNotIsland(updateProposalRequestDto.getIsland())) {
            throw new InvalidFieldException("The island field is invalid");
        }
        if(Location.isNotZipCode(updateProposalRequestDto.getZipCode())) {
            throw new InvalidFieldException("The zip code field is invalid");
        }
        if (updateProposalRequestDto.getTitle().length() > 75) {
            throw new InvalidFieldException("The additional information field is invalid");
        }
        if (updateProposalRequestDto.getDescription().length() > 200) {
            throw new InvalidFieldException("The description field is invalid");
        }
        if (updateProposalRequestDto.getExtraInfo() != null && updateProposalRequestDto.getExtraInfo().length() > 200) {
            throw new InvalidFieldException("The extra info field is invalid");
        }
        if (updateProposalRequestDto.getInstructions() != null && updateProposalRequestDto.getInstructions().length() > 200) {
            throw new InvalidFieldException("The instruction field is invalid");
        }
        if (updateProposalRequestDto.getStartingProposalDate() != null &&
                updateProposalRequestDto.getClosingProposalDate().isBefore(updateProposalRequestDto.getStartingProposalDate())) {
            throw new InvalidFieldException("The closing recruitment can not before of the starting proposal");
        }
        if (updateProposalRequestDto.getStartingVolunteeringDate().isBefore(updateProposalRequestDto.getClosingProposalDate())) {
            throw new InvalidFieldException("The starting volunteering can not before of the closing recruitment");
        }
    }

    /**
     * In this method delete the old skills and add the new skills in the proposal.
     * This method is a bit ugly by the loops, it is to avoid concurrency exceptions,
     * is not possible read and delete information at the same time.
     *
     * @param updateProposalRequestDto The new information about the proposal
     * @param proposal The proposal to update
     */
    private void addNewRequeriments(UpdateProposalRequestDto updateProposalRequestDto, Proposal proposal) {
        List<Requirement> deleteRequirements = new ArrayList<>(proposal.getRequirements());
        for (Requirement requirement : deleteRequirements) {
            proposal.deleteRequirement(requirement);
        }
        if (updateProposalRequestDto.getRequirements() != null) {
            updateProposalRequestDto.getRequirements().stream()
                    .map(Requirement::new)
                    .forEach(proposal::addRequirement);
        }
    }

    /**
     * In this method delete the old skills and add the new skills in the proposal.
     * This method is a bit ugly by the loops, it is to avoid concurrency exceptions,
     * is not possible read and delete information at the same time.
     *
     * @param updateProposalRequestDto The new information about the proposal
     * @param proposal The proposal to update
     */
    private void addNewSkills(UpdateProposalRequestDto updateProposalRequestDto, Proposal proposal) {
        List<Skill> deleteSkills = new ArrayList<>(proposal.getSkills());
        for (Skill skill : deleteSkills) {
            proposal.deleteSkill(skill);
        }
        Optional.ofNullable(updateProposalRequestDto.getSkills())
                .orElse(emptyList())
                .stream()
                .map(skillDto -> new Skill(skillDto.getName(), skillDto.getDescription()))
                .forEach(proposal::addSkill);
    } // TODO Set orphanChild when cascading *ToMany relation

    /**
     * Check for feedback message
     *
     * @param proposalRevisionDto Contains the email reviser and the feedback if has it.
     */
    private boolean hasFeedback(ProposalRevisionDto proposalRevisionDto) {
        return proposalRevisionDto.getFeedback() != null;
    }

    /**
     * Close the enrollment of the provided proposal.
     * @param idProposal : The id of the proposal to be checked and updated.
     * @throws ProposalEnrollmentNotCloseableException when the proposal is not published.
     */
    public void closeEnrollment(String idProposal) {
        JpaProposal proposal = jpaProposalRepository.findByNaturalId(idProposal).orElseThrow(EntityNotFoundException::new);
        String status = proposal.getStatus().getName().toUpperCase();

        if (!PUBLISHED.toString().equals(status)) {
            throw new ProposalEnrollmentNotCloseableException();
        }

        JpaProposalStatus jpaProposalStatus = JpaProposalStatus.builder()
                .id(ENROLLMENT_CLOSED.getId())
                .name("ENROLLMENT_CLOSED").build();
        jpaProposalRepository.updateStatusById(idProposal, jpaProposalStatus);
    }

    /**
     * This method find the proposal in the database and checks if the status is REVIEW_PENDING or ENROLLMENT_CLOSED for
     * publish. Otherwise, a ProposalNotPublishableException with response status 409 will be throw.
     * @param proposalId : The id of the proposal to be checked and updated.
     * @return result with the proposal person email and proposal title.
     */
    public ChangeStatusToPublishedResult changeStatusToPublished(String proposalId) {
        JpaProposal proposal = jpaProposalRepository.findByNaturalId(proposalId).orElseThrow(EntityNotFoundException::new);
        String esalId = proposal.getEsal().getId();
        String status = proposal.getStatus().getName().toUpperCase();

        if (!REVIEW_PENDING.toString().equals(status) && !ENROLLMENT_CLOSED.toString().equals(status)) {
            throw new ProposalNotPublishableException();
        }

        JpaProposalStatus jpaProposalStatus = JpaProposalStatus.builder()
                .id(ProposalStatus.PUBLISHED.getId())
                .name("PUBLISHED").build();
        jpaProposalRepository.updateStatusById(proposalId, jpaProposalStatus);

        JpaContactPerson contactPerson = jpaContactPersonRepository.findByEsalId(esalId).orElseThrow(EntityNotFoundException::new);
        return new ChangeStatusToPublishedResult(contactPerson.getCredential().getEmail(), proposal.getTitle());
    }

    /**
     * Publish proposal if the proposal have status ENROLLMENT_CLOSED.
     * @param proposalId : The id of the proposal to be checked and updated.
     * @throws - ProposalNotPublishableException
     */
    public void changeStatusToPublishedFromEnrollmentClosed(String proposalId) {
        JpaProposal proposal = jpaProposalRepository.findByNaturalId(proposalId).orElseThrow(EntityNotFoundException::new);
        String status = proposal.getStatus().getName().toUpperCase();

        if (!ENROLLMENT_CLOSED.toString().equals(status)) {
            throw new ProposalNotPublishableException();
        }

        JpaProposalStatus jpaProposalStatus = JpaProposalStatus.builder()
                .id(ProposalStatus.PUBLISHED.getId())
                .name("PUBLISHED").build();
        jpaProposalRepository.updateStatusById(proposalId, jpaProposalStatus);
    }

    @Scheduled(cron = "0 ${random.int[0,59]} 0 * * ?")
    public void changeExpiredProposalStatusToFinished() {

        JpaProposalStatus jpaProposalStatusFinished = JpaProposalStatus.builder()
                .id(FINISHED.getId())
                .name("FINISHED")
                .build();

        List<JpaProposal> expiredProposals = jpaProposalRepository
                .findExpiredProposals(Date.from(Instant.now()), jpaProposalStatusFinished);

        expiredProposals.forEach(
                jpaProposal -> jpaProposalRepository.updateStatusById(jpaProposal.getId(), jpaProposalStatusFinished));
    }

    public void changeStatusToFinished(String proposalId) {

        JpaProposal proposal = jpaProposalRepository.findByNaturalId(proposalId)
                .orElseThrow(EntityNotFoundException::new);
        String status = proposal.getStatus().getName().toUpperCase();

        if (!(PUBLISHED.toString().equals(status) || ENROLLMENT_CLOSED.toString().equals(status))) {
            throw new InvalidProposalStatusException("Proposal must be either PUBLISHED or ENROLLMENT_CLOSED");
        }

        JpaProposalStatus jpaProposalStatus = JpaProposalStatus.builder()
                .id(FINISHED.getId())
                .name("FINISHED")
                .build();

        jpaProposalRepository.updateStatusById(proposalId, jpaProposalStatus);
    }

}
