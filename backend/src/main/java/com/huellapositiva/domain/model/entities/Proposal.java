package com.huellapositiva.domain.model.entities;


import com.huellapositiva.application.dto.ProposalRequestDto;
import com.huellapositiva.domain.exception.InvalidProposalRequestException;
import com.huellapositiva.domain.model.valueobjects.*;
import com.huellapositiva.infrastructure.orm.entities.JpaProposal;
import lombok.*;

import javax.validation.constraints.NotEmpty;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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

    private final List<Skill> skills = new ArrayList<>();

    private final List<Requirement> requirements = new ArrayList<>();

    private URL image;

    private ProposalStatus status;

    public void inscribeVolunteer(Volunteer volunteer) {
        inscribedVolunteers.add(volunteer);
    }

    public void addSkill(Skill skill) {
        skills.add(skill);
    }

    /**
     * This method delete the skills if to find the same
     *
     * @param skill To check if it is on the list or not
     */
    public void deleteSkill(Skill skill) {
        int positionOfSkill = -1;
        for (Skill skillProposal : skills) {
            if (skill.getName().equals(skillProposal.getName())){
                positionOfSkill = skills.indexOf(skillProposal);
                break;
            }
        }
        skills.remove(positionOfSkill);
    }

    public void addRequirement(Requirement requirement) {
        requirements.add(requirement);
    }

    /**
     * This method delete the requirement if to find the same
     *
     * @param requirement To check if it is on the list or not
     */
    public void deleteRequirement(Requirement requirement) {
        int positionOfRequirement = -1;
        for (Requirement requirementProposal : requirements) {
            if (requirement.getName().equals(requirementProposal.getName())){
                positionOfRequirement = requirements.indexOf(requirementProposal);
                break;
            }
        }
        requirements.remove(positionOfRequirement);
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

        Arrays.stream(dto.getSkills())
                .forEach(s -> proposal.addSkill( new Skill(s[0], s[1])));
        Arrays.asList(dto.getRequirements())
                .forEach(r -> proposal.addRequirement(new Requirement(r)));

        return proposal;
    }

    @SneakyThrows
    public static Proposal parseJpa(JpaProposal jpaProposal) {
        return Proposal.builder()
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
    }

    public void validate(){
        boolean closingBeforeStartingProposal = closingProposalDate.isBefore(startingProposalDate);
        boolean startingVolunteeringBeforeClosing = startingVolunteeringDate.isBefore(closingProposalDate);
        if(closingBeforeStartingProposal || startingVolunteeringBeforeClosing){
            throw new InvalidProposalRequestException("Date is not in a valid range.");
        }
        if(startingProposalDate.getBusinessDaysFrom(new Date()) < 3){
            throw new InvalidProposalRequestException("Proposal must start at least within three business days from today.");
        }
        if(closingProposalDate.isNotBeforeStipulatedDeadline()){
            throw new InvalidProposalRequestException("Proposal deadline must be less than six months from now.");
        }
    }
}
