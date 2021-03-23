package com.huellapositiva.domain.model.entities;


import com.huellapositiva.application.dto.ProposalRequestDto;
import com.huellapositiva.domain.exception.InvalidProposalRequestException;
import com.huellapositiva.domain.model.valueobjects.*;
import com.huellapositiva.infrastructure.orm.entities.JpaProposal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;

import javax.validation.constraints.NotEmpty;
import java.net.URL;
import java.text.ParseException;
import java.util.*;

@Builder
@Data
@AllArgsConstructor
public class Proposal {

    private Integer surrogateKey;

    @NotEmpty
    private final Id id;

    @NotEmpty
    private String title;

    @NotEmpty
    private ESAL esal;

    @NotEmpty
    private Location location;

    @NotEmpty
    private String requiredDays;

    @NotEmpty
    private AgeRange permittedAgeRange;

    @NotEmpty
    private ProposalDate startingProposalDate;

    @NotEmpty
    private ProposalDate closingProposalDate;

    @NotEmpty
    private ProposalDate startingVolunteeringDate;

    @NotEmpty
    private String description;

    @NotEmpty
    private String durationInDays;

    @NotEmpty
    private ProposalCategory category;

    @NotEmpty
    private String extraInfo;

    @NotEmpty
    private String instructions;

    private final List<Volunteer> inscribedVolunteers = new ArrayList<>();

    private final Set<Skill> skills = new HashSet<>();

    private final Set<Requirement> requirements = new HashSet<>();

    private URL image;

    private ProposalStatus status;

    public void inscribeVolunteer(Volunteer volunteer) {
        inscribedVolunteers.add(volunteer);
    }

    public void addSkill(Skill skill) {
        skills.add(skill);
    }

    /**
     * Delete a skill from a proposal if present.
     *
     * @param skill Skill to delete
     */
    public void deleteSkill(Skill skill) {
        skills.remove(skill);
    }

    public void addRequirement(Requirement requirement) {
        requirements.add(requirement);
    }

    /**
     * Delete a requirement if present.
     *
     * @param requirement Requirement to delete
     */
    public void deleteRequirement(Requirement requirement) {
        requirements.remove(requirement);
    }

    public static Proposal parseDto(ProposalRequestDto dto, ESAL joinedESAL) throws ParseException {
        Proposal proposal = Proposal.builder()
                .id(Id.newId())
                .title(dto.getTitle())
                .esal(joinedESAL)
                .startingProposalDate(ProposalDate.createStartingProposalDate(dto.getStartingProposalDate()))
                .closingProposalDate(ProposalDate.createClosingProposalDate(dto.getClosingProposalDate()))
                .permittedAgeRange(AgeRange.create(dto.getMinimumAge(), dto.getMaximumAge()))
                .location(new Location(
                        dto.getProvince(),
                        dto.getTown(),
                        dto.getAddress(),
                        dto.getZipCode(),
                        dto.getIsland()))
                .requiredDays(dto.getRequiredDays())
                .description(dto.getDescription())
                .durationInDays(dto.getDurationInDays())
                .category(ProposalCategory.valueOf(dto.getCategory()))
                .startingVolunteeringDate(ProposalDate.createStartingVolunteeringDate(dto.getStartingVolunteeringDate()))
                .extraInfo(dto.getExtraInfo())
                .instructions(dto.getInstructions())
                .build();

        dto.getSkills().stream()
                .map(skillDto -> new Skill(skillDto.getName(), skillDto.getDescription()))
                .forEach(proposal::addSkill);
        dto.getRequirements().stream()
                .map(Requirement::new)
                .forEach(proposal::addRequirement);

        proposal.validate();
        return proposal;
    }

    @SneakyThrows
    public static Proposal parseJpa(JpaProposal jpaProposal) {
        Proposal proposal = Proposal.builder()
                .surrogateKey(jpaProposal.getSurrogateKey())
                .id(new Id(jpaProposal.getId()))
                .esal(ESAL.fromJpa(jpaProposal.getEsal()))
                .title(jpaProposal.getTitle())
                .location(new Location(
                        jpaProposal.getLocation().getProvince(),
                        jpaProposal.getLocation().getTown(),
                        jpaProposal.getLocation().getAddress(),
                        jpaProposal.getLocation().getZipCode(),
                        jpaProposal.getLocation().getIsland()))
                .startingProposalDate(new ProposalDate(jpaProposal.getStartingProposalDate()))
                .closingProposalDate(new ProposalDate(jpaProposal.getClosingProposalDate()))
                .startingVolunteeringDate(new ProposalDate(jpaProposal.getStartingVolunteeringDate()))
                .permittedAgeRange(AgeRange.create(jpaProposal.getMinimumAge(), jpaProposal.getMaximumAge()))
                .requiredDays(jpaProposal.getRequiredDays())
                .status(ProposalStatus.getStatus(jpaProposal.getStatus().getId()))
                .description(jpaProposal.getDescription())
                .durationInDays(jpaProposal.getDurationInDays())
                .category(ProposalCategory.valueOf(jpaProposal.getCategory()))
                .extraInfo(jpaProposal.getExtraInfo())
                .instructions(jpaProposal.getInstructions())
                .image(jpaProposal.getImageUrl() != null ? new URL(jpaProposal.getImageUrl()) : null)
                .build();

        jpaProposal.getInscribedVolunteers().stream()
                .map(v -> new Volunteer(new Id(v.getCredential().getId()), EmailAddress.from(v.getCredential().getEmail()), new Id(v.getId())))
                .forEach(proposal::inscribeVolunteer);
        jpaProposal.getSkills().stream()
                .map(jpaSkill -> new Skill(jpaSkill.getName(), jpaSkill.getDescription()))
                .forEach(proposal::addSkill);
        jpaProposal.getRequirements().stream()
                .map(jpaRequirement -> new Requirement(jpaRequirement.getName()))
                .forEach(proposal::addRequirement);

        return proposal;
    }

    public void validate() {
        if(permittedAgeRange.getMinimum() < 18 || permittedAgeRange.getMaximum() > 80) {
            throw new InvalidProposalRequestException("Age is not in a valid range [0,80]");
        }
        if (permittedAgeRange.getMinimum() > permittedAgeRange.getMaximum()) {
            throw new InvalidProposalRequestException("Minimum age cannot be greater than maximum age.");
        }
        if(closingProposalDate.isBefore(startingProposalDate) || startingVolunteeringDate.isBefore(closingProposalDate)) {
            throw new InvalidProposalRequestException("Date is not in a valid range.");
        }
        if(startingProposalDate.getBusinessDaysFrom(new Date()) < 3) {
            throw new InvalidProposalRequestException("Proposal must start at least within three business days from today.");
        }
        if(closingProposalDate.isNotBeforeStipulatedDeadline()) {
            throw new InvalidProposalRequestException("Proposal deadline must be less than six months from now.");
        }
    }
}
