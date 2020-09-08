package com.huellapositiva.domain.model.entities;


import com.huellapositiva.application.dto.ProposalRequestDto;
import com.huellapositiva.domain.exception.InvalidProposalRequestException;
import com.huellapositiva.domain.model.valueobjects.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.net.URL;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class Proposal {

    private Integer surrogateKey;

    @NotEmpty
    private final Id id;

    @NotEmpty
    private final String title;

    @NotEmpty
    private final ESAL esal;

    @NotEmpty
    private final Location location;

    @NotEmpty
    private final String requiredDays;

    @NotEmpty
    private final AgeRange permittedAgeRange;

    @NotEmpty
    private final ProposalDate startingProposalDate;

    @NotEmpty
    private final ProposalDate closingProposalDate;

    @NotEmpty
    private final ProposalDate startingVolunteeringDate;

    @NotEmpty
    private final String description;

    @NotEmpty
    private final String durationInDays;

    @NotEmpty
    private final ProposalCategory category;

    @NotEmpty
    private final String extraInfo;

    @NotEmpty
    private final String instructions;

    private final List<Volunteer> inscribedVolunteers = new ArrayList<>();

    private final List<Skill> skills = new ArrayList<>();

    private final List<Requirement> requirements = new ArrayList<>();

    private URL image;

    private boolean published;

    public void inscribeVolunteer(Volunteer volunteer) {
        inscribedVolunteers.add(volunteer);
    }

    public void addSkill(Skill skill) {
        skills.add(skill);
    }

    public void addRequirement(Requirement requirement) {
        requirements.add(requirement);
    }

    public static Proposal parseDto(ProposalRequestDto dto, ESAL joinedESAL) throws ParseException {
        Proposal proposal = Proposal.builder()
                .id(Id.newId())
                .title(dto.getTitle())
                .esal(joinedESAL)
                .startingProposalDate(ProposalDate.createStartingProposalDate(dto.getStartingProposalDate()))
                .closingProposalDate(ProposalDate.createClosingProposalDate(dto.getClosingProposalDate()))
                .permittedAgeRange(AgeRange.create(dto.getMinimumAge(), dto.getMaximumAge()))
                .location(new Location(dto.getProvince(), dto.getTown(), dto.getAddress()))
                .requiredDays(dto.getRequiredDays())
                .published(dto.isPublished())
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

    public void validate(){
        boolean closingBeforeStartingProposal = closingProposalDate.isBefore(startingProposalDate);
        boolean startingVolunteeringBeforeClosing = startingVolunteeringDate.isBefore(closingProposalDate);
        if(closingBeforeStartingProposal || startingVolunteeringBeforeClosing){
            throw new InvalidProposalRequestException("Date is not in a valid range.");
        }
        if(startingProposalDate.getBusinessDaysFrom(new Date()) < 3){
            throw new InvalidProposalRequestException("Proposal must start at least within three business days from today.");
        }
        Date dateSixMonthsFromNow = Date.from(Instant.now().plus(6, ChronoUnit.MONTHS));
        if(!closingProposalDate.getDate().before(dateSixMonthsFromNow)){
            throw new InvalidProposalRequestException("Proposal deadline must be less than six months from now.");
        }
    }
}
