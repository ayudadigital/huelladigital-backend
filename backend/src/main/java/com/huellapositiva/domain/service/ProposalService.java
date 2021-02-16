package com.huellapositiva.domain.service;

import com.huellapositiva.application.dto.ProposalRevisionDto;
import com.huellapositiva.application.dto.UpdateProposalRequestDto;
import com.huellapositiva.application.exception.ProposalEnrollmentClosedException;
import com.huellapositiva.application.exception.ProposalNotPublishedException;
import com.huellapositiva.domain.exception.InvalidProposalStatusException;
import com.huellapositiva.domain.exception.StatusNotFoundException;
import com.huellapositiva.domain.model.entities.*;
import com.huellapositiva.domain.model.valueobjects.*;
import com.huellapositiva.domain.repository.ContactPersonRepository;
import com.huellapositiva.domain.repository.CredentialsRepository;
import com.huellapositiva.domain.repository.ProposalRepository;
import com.huellapositiva.infrastructure.orm.entities.JpaProposal;
import com.huellapositiva.infrastructure.orm.entities.JpaProposalStatus;
import com.huellapositiva.infrastructure.orm.repository.JpaProposalRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaProposalStatusRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.huellapositiva.domain.model.valueobjects.ProposalStatus.CHANGES_REQUESTED;
import static com.huellapositiva.domain.model.valueobjects.ProposalStatus.PUBLISHED;

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

    public void updateProposal(UpdateProposalRequestDto updateProposalRequestDto) throws ParseException {
        Proposal proposal = proposalRepository.fetch(updateProposalRequestDto.getId());
        /*JpaProposal jpaProposal = jpaProposalRepository.findByNaturalId(updateProposalRequestDto.getId())
                .orElseThrow(EntityNotFoundException::new);*/

        /* Validación si la edad es número */
        /* Validación si las fechas tienen sentido */
        /* Validación de proposalCategory, es un ENUM */
        /* Las skills son parte de la base de datos*/
        /* Los requeriments también son parte de la base de datos*/

        proposal.setTitle(updateProposalRequestDto.getTitle());
        proposal.getEsal().setName(updateProposalRequestDto.getEsalName());
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
        proposal.setStartingProposalDate(
                ProposalDate.createStartingProposalDate(updateProposalRequestDto.getStartingProposalDate())
        );
        proposal.setStartingProposalDate(
                ProposalDate.createClosingProposalDate(updateProposalRequestDto.getStartingProposalDate())
        );
        proposal.setStartingProposalDate(
                ProposalDate.createStartingVolunteeringDate(updateProposalRequestDto.getStartingProposalDate())
        );
        proposal.setDescription(updateProposalRequestDto.getDescription());
        proposal.setDurationInDays(updateProposalRequestDto.getDurationInDays());

        if ("ON_SITE".equals(updateProposalRequestDto.getCategory())) {
            proposal.setCategory(ProposalCategory.ON_SITE);
        } else if ("REMOTE".equals(updateProposalRequestDto.getCategory())) {
            proposal.setCategory(ProposalCategory.REMOTE);
        } else {
            proposal.setCategory(ProposalCategory.MIXED);
        }

        proposal.setExtraInfo(updateProposalRequestDto.getExtraInfo());
        proposal.setInstructions(updateProposalRequestDto.getInstructions());

        /* Es para saltarme una excepción de concurrencia*/
        List<Skill> deleteSkills = new ArrayList<>();
        for (Skill skill : proposal.getSkills()) {
            String name = skill.getName();
            String description = skill.getDescription();
            Skill skillToKill = new Skill(name, description);
            deleteSkills.add(skillToKill);
        }
        for (Skill skill : deleteSkills) {
            proposal.deleteSkill(skill);
        }
        for (String[] skill : updateProposalRequestDto.getSkills()) {
            Skill newSkill = new Skill(skill[0], skill[1]);
            proposal.addSkill(newSkill);
        }
        /*---------------------------*/

        /* Es para saltarme una excepción de concurrencia*/
        List<Requirement> deleteRequirements = new ArrayList<>();
        for (Requirement requirement : proposal.getRequirements()) {
            String name = requirement.getName();
            Requirement requirementToKill = new Requirement(name);
            deleteRequirements.add(requirementToKill);
        }
        for (Requirement requirement : deleteRequirements) {
            proposal.deleteRequeriment(requirement);
        }
        for (String requirement : updateProposalRequestDto.getRequirements()) {
            Requirement newRequirement = new Requirement(requirement);
            proposal.addRequirement(newRequirement);
        }
        /*---------------------------*/

        System.out.println("Hola");

        /*Arrays.stream(updateProposalRequestDto.getSkills())
                .forEach(s -> {
                    if (proposal.getSkills().contains(s[0])) {
                        proposal.addSkill(new Skill(s[0], s[1]));
                    }
                });

        Arrays.stream(updateProposalRequestDto.getRequirements())
                .forEach(r -> {
                    if (proposal.getRequirements().contains(r)) {
                        proposal.addRequirement(new Requirement(r));
                    }
                });*/
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
