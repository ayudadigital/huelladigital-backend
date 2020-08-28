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
import java.util.ArrayList;
import java.util.Arrays;
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
    private final AgeRange permitedAgeRange;

    @NotEmpty
    private final ProposalDate expirationDate;

    @NotEmpty
    private final ProposalDate startingDate;

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
                .expirationDate(ProposalDate.createExpirationDate(dto.getExpirationDate()))
                .permitedAgeRange(AgeRange.create(dto.getMinimumAge(), dto.getMaximumAge()))
                .location(new Location(dto.getProvince(), dto.getTown(), dto.getAddress()))
                .requiredDays(dto.getRequiredDays())
                .published(dto.isPublished())
                .description(dto.getDescription())
                .durationInDays(dto.getDurationInDays())
                .category(ProposalCategory.valueOf(dto.getCategory()))
                .startingDate(ProposalDate.createStartingDate(dto.getStartingDate()))
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
        if(expirationDate.isBeforeNow() || startingDate.isBefore(expirationDate)){
            throw new InvalidProposalRequestException("Date is not in a valid range.");
        }
    }
}
