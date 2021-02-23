package com.huellapositiva.domain.service;

import com.huellapositiva.application.dto.ProposalRevisionDto;
import com.huellapositiva.application.dto.UpdateProposalRequestDto;
import com.huellapositiva.application.exception.ProposalEnrollmentClosedException;
import com.huellapositiva.application.exception.ProposalNotLinkedWithContactPersonException;
import com.huellapositiva.application.exception.ProposalNotPublishedException;
import com.huellapositiva.application.exception.UserNotFoundException;
import com.huellapositiva.domain.exception.InvalidProposalStatusException;
import com.huellapositiva.domain.exception.StatusNotFoundException;
import com.huellapositiva.domain.model.entities.*;
import com.huellapositiva.domain.model.valueobjects.*;
import com.huellapositiva.domain.repository.ContactPersonRepository;
import com.huellapositiva.domain.repository.CredentialsRepository;
import com.huellapositiva.domain.repository.ProposalRepository;
import com.huellapositiva.infrastructure.orm.entities.JpaCredential;
import com.huellapositiva.infrastructure.orm.entities.JpaProposalStatus;
import com.huellapositiva.infrastructure.orm.repository.JpaCredentialRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaProposalRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaProposalStatusRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static com.huellapositiva.domain.model.valueobjects.ProposalStatus.*;

@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class ProposalService {

    private final ProposalRepository proposalRepository;

    private final ContactPersonRepository contactPersonRepository;

    private final CredentialsRepository credentialsRepository;

    private final JpaProposalRepository jpaProposalRepository;

    private final JpaProposalStatusRepository jpaProposalStatusRepository;

    private final JpaCredentialRepository jpaCredentialRepository;

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

    public void updateProposal(UpdateProposalRequestDto updateProposalRequestDto, String accountId) throws ParseException {
        /* Crear método para validaciones en el action (y las más especificas en su valueObject correspondiente) */
        /* Validación si la edad es número */
        /* Validación si las fechas tienen sentido */
        /* Validación de proposalCategory, es un ENUM */

        Proposal proposal = proposalRepository.fetch(updateProposalRequestDto.getId());
        JpaCredential jpaCredential = jpaCredentialRepository.findByAccountId(accountId)
                .orElseThrow(() -> new UserNotFoundException("Volunteer not found. Account ID: " + accountId));

        /*
        Esto es por si al usuario se le ocurriera modificar el JavaScript
        */
        if(!(jpaCredential.getEmail().equals(proposal.getEsal().getContactPersonEmail().toString()))){
            throw new ProposalNotLinkedWithContactPersonException("This proposal not linked with your account");
        }

        proposal.setTitle(updateProposalRequestDto.getTitle());
        proposal.getLocation().setProvince(updateProposalRequestDto.getProvince());
        /*Validar*/
        proposal.getLocation().setIsland(updateProposalRequestDto.getIsland());
        proposal.getLocation().setTown(updateProposalRequestDto.getTown());
        /*Validar*/
        proposal.getLocation().setZipCode(updateProposalRequestDto.getZipCode());
        proposal.getLocation().setAddress(updateProposalRequestDto.getAddress());
        proposal.setRequiredDays(updateProposalRequestDto.getRequiredDays());
        /*Ya está validado*/
        proposal.setPermittedAgeRange(
                AgeRange.create(
                        updateProposalRequestDto.getMinimumAge(),
                        updateProposalRequestDto.getMaximumAge()
                )
        );
        /*Validar con ProposalDate*/
        proposal.setStartingProposalDate(
                ProposalDate.createStartingProposalDate(updateProposalRequestDto.getStartingProposalDate().toString())
        );
        proposal.setStartingProposalDate(
                ProposalDate.createClosingProposalDate(updateProposalRequestDto.getStartingProposalDate().toString())
        );
        proposal.setStartingProposalDate(
                ProposalDate.createStartingVolunteeringDate(updateProposalRequestDto.getStartingProposalDate().toString())
        );
        /*Validar con longitud máxima*/
        proposal.setDescription(updateProposalRequestDto.getDescription());
        /*Validar como número*/
        proposal.setDurationInDays(updateProposalRequestDto.getDurationInDays());

        if ("ON_SITE".equals(updateProposalRequestDto.getCategory())) {
            proposal.setCategory(ProposalCategory.ON_SITE);
        } else if ("REMOTE".equals(updateProposalRequestDto.getCategory())) {
            proposal.setCategory(ProposalCategory.REMOTE);
        } else {
            proposal.setCategory(ProposalCategory.MIXED);
        }

        /*Validar con longitud máxima*/
        proposal.setExtraInfo(updateProposalRequestDto.getExtraInfo());
        /*Validar con longitud máxima*/
        proposal.setInstructions(updateProposalRequestDto.getInstructions());

        addNewSkills(updateProposalRequestDto, proposal);
        addNewRequeriments(updateProposalRequestDto, proposal);

        proposalRepository.update(proposal);
    }

    private void addNewRequeriments(UpdateProposalRequestDto updateProposalRequestDto, Proposal proposal) {
        /* Es para saltarme una excepción de concurrencia*/
        List<Requirement> deleteRequirements = new ArrayList<>();
        for (Requirement requirement : proposal.getRequirements()) {
            deleteRequirements.add(requirement);
        }
        for (Requirement requirement : deleteRequirements) {
            proposal.deleteRequeriment(requirement);
        }
        for (String requirement : updateProposalRequestDto.getRequirements()) {
            Requirement newRequirement = new Requirement(requirement);
            proposal.addRequirement(newRequirement);
        }
    }

    private void addNewSkills(UpdateProposalRequestDto updateProposalRequestDto, Proposal proposal) {
        /* Es para saltarme una excepción de concurrencia*/
        List<Skill> deleteSkills = new ArrayList<>();
        for (Skill skill : proposal.getSkills()) {
            deleteSkills.add(skill);
        }
        for (Skill skill : deleteSkills) {
            proposal.deleteSkill(skill);
        }
        for (String[] skill : updateProposalRequestDto.getSkills()) {
            Skill newSkill = new Skill(skill[0], skill[1]);
            proposal.addSkill(newSkill);
        }
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
