package com.huellapositiva.domain.model.entities;


import com.huellapositiva.application.dto.ProposalRequestDto;
import com.huellapositiva.domain.exception.InvalidProposalRequestException;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.domain.model.valueobjects.Location;
import com.huellapositiva.domain.model.valueobjects.ProposalCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.validation.constraints.NotEmpty;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
    private final int minimumAge;

    @NotEmpty
    private final int maximumAge;

    @NotEmpty
    private final Date expirationDate;

    private boolean published;

    @NotEmpty
    private final String description;

    @NotEmpty
    private final String durationInDays;

    @NotEmpty
    private final ProposalCategory category;

    @NotEmpty
    private final Date startingDate;

    private final List<Volunteer> inscribedVolunteers = new ArrayList<>();

    private final List<Pair<String, String>> skills = new ArrayList<>();

    private final List<String> requirements = new ArrayList<>();

    private final String extraInfo;

    private final String instructions;

    public void inscribeVolunteer(Volunteer volunteer) {
        inscribedVolunteers.add(volunteer);
    }

    public void addSkill(String name, String description) {
        skills.add(new MutablePair<>(name, description));
    }

    public void addRequirement(String name) {
        requirements.add(name);
    }

    public static Proposal parseDto(ProposalRequestDto dto, ESAL joinedESAL) throws ParseException {
        Proposal proposal = Proposal.builder()
                .id(Id.newId())
                .title(dto.getTitle())
                .esal(joinedESAL)
                .expirationDate(new SimpleDateFormat("dd-MM-yyyy").parse(dto.getExpirationDate()))
                .maximumAge(dto.getMaximumAge())
                .minimumAge(dto.getMinimumAge())
                .location(new Location(dto.getProvince(), dto.getTown(), dto.getAddress()))
                .requiredDays(dto.getRequiredDays())
                .published(dto.isPublished())
                .description(dto.getDescription())
                .durationInDays(dto.getDurationInDays())
                .category(ProposalCategory.valueOf(dto.getCategory()))
                .startingDate(new SimpleDateFormat("dd-MM-yyyy").parse(dto.getStartingDate()))
                .extraInfo(dto.getExtraInfo())
                .instructions(dto.getInstructions())
                .build();

        Arrays.stream(dto.getSkills())
                .map(s -> new MutablePair<>(s[0], s[1]))
                .collect(Collectors.toList())
                .forEach(p -> proposal.addSkill(p.getKey(), p.getValue()));
        Arrays.asList(dto.getRequirements())
                .forEach(proposal::addRequirement);

        return proposal;
    }

    public void validate(){
        boolean isExpirationDatePastDate = expirationDate.before(new Date());
        boolean isStartingDateBeforeExpirationDate = startingDate.before(expirationDate);
        if(isExpirationDatePastDate || isStartingDateBeforeExpirationDate){
            throw new InvalidProposalRequestException("Date is not in a valid range.");
        }
        if(minimumAge < 18 || maximumAge > 55){
            throw new InvalidProposalRequestException("Age is not in a valid range.");
        }
        if(minimumAge > maximumAge){
            throw new InvalidProposalRequestException("Minimum age cannot be greater than maximum age.");
        }
    }
}
