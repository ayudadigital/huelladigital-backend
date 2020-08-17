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

import javax.validation.constraints.NotEmpty;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private final int minimumAge;

    @NotEmpty
    private final int maximumAge;

    @NotEmpty
    private final Date expirationDate;

    private boolean published;

    @NotEmpty
    private final String description;

    @NotEmpty
    private final Integer durationInDays;

    @NotEmpty
    private final ProposalCategory category;

    @NotEmpty
    private final Date startingDate;

    private final List<Volunteer> inscribedVolunteers = new ArrayList<>();

    public void inscribeVolunteer(Volunteer volunteer) {
        inscribedVolunteers.add(volunteer);
    }

    public static Proposal parseDto(ProposalRequestDto dto, ESAL joinedESAL) throws ParseException {
        return Proposal.builder()
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
                .build();
    }

    public void validate(){
        boolean isExpirationDatePastDate = expirationDate.before(new Date());
        boolean isStartingDateBeforeExpirationDate = startingDate.before(expirationDate);
        if(isExpirationDatePastDate || isStartingDateBeforeExpirationDate){
            throw new InvalidProposalRequestException("Date is not in a valid range.");
        }
        if(minimumAge < 18 || maximumAge > 65){
            throw new InvalidProposalRequestException("Age is not in a valid range.");
        }
        if(minimumAge > maximumAge){
            throw new InvalidProposalRequestException("Minimum age cannot be greater than maximum age.");
        }
    }
}
