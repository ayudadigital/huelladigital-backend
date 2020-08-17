package com.huellapositiva.domain.repository;

import com.huellapositiva.application.exception.ESALNotFound;
import com.huellapositiva.domain.model.entities.ESAL;
import com.huellapositiva.domain.model.entities.Proposal;
import com.huellapositiva.domain.model.entities.Volunteer;
import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.domain.model.valueobjects.Location;
import com.huellapositiva.domain.model.valueobjects.ProposalCategory;
import com.huellapositiva.infrastructure.orm.entities.JpaESAL;
import com.huellapositiva.infrastructure.orm.entities.JpaLocation;
import com.huellapositiva.infrastructure.orm.entities.JpaProposal;
import com.huellapositiva.infrastructure.orm.entities.JpaVolunteer;
import com.huellapositiva.infrastructure.orm.repository.JpaESALRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaLocationRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaProposalRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaVolunteerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Transactional
@RequiredArgsConstructor
public class ProposalRepository {

    @Autowired
    private final JpaLocationRepository jpaLocationRepository;

    @Autowired
    private final JpaProposalRepository jpaProposalRepository;

    @Autowired
    private final JpaESALRepository jpaESALRepository;

    @Autowired
    private final JpaVolunteerRepository jpaVolunteerRepository;

    @Value("${huellapositiva.proposal.expiration-hour}")
    private Integer expirationHour;

    @Value("${huellapositiva.proposal.expiration-minute}")
    private Integer expirationMinute;

    public String save(Proposal proposal) {
        JpaLocation jpaLocation = jpaLocationRepository.save(JpaLocation.builder()
                .id(proposal.getLocation().getId().toString())
                .province(proposal.getLocation().getProvince())
                .town(proposal.getLocation().getTown())
                .address(proposal.getLocation().getAddress())
                .build());

        Date expirationDate = Date.from(proposal.getExpirationDate().toInstant().plus(expirationHour, ChronoUnit.HOURS).plus(expirationMinute, ChronoUnit.MINUTES));
        JpaESAL esal = jpaESALRepository.findByName(proposal.getEsal().getName())
                .orElseThrow(ESALNotFound::new);
        Set<JpaVolunteer> volunteers = proposal.getInscribedVolunteers()
                .stream()
                .map(v -> jpaVolunteerRepository.findByIdWithCredentialsAndRoles(v.getId().toString()).get())
                .collect(Collectors.toSet());
        JpaProposal jpaProposal = JpaProposal.builder()
                .id(proposal.getId().toString())
                .title(proposal.getTitle())
                .esal(esal)
                .location(jpaLocation)
                .expirationDate(expirationDate)
                .requiredDays(proposal.getRequiredDays())
                .minimumAge(proposal.getMinimumAge())
                .maximumAge(proposal.getMaximumAge())
                .published(proposal.isPublished())
                .description(proposal.getDescription())
                .durationInDays(proposal.getDurationInDays())
                .startingDate(proposal.getStartingDate())
                .category(proposal.getCategory().toString())
                .inscribedVolunteers(volunteers)
                .build();

        if (proposal.getSurrogateKey() != null) {
            jpaProposal.setSurrogateKey(proposal.getSurrogateKey());
        }
        return jpaProposalRepository.save(jpaProposal).getId();
    }

    public JpaProposal save(JpaProposal proposal) {
        return jpaProposalRepository.save(proposal);
    }

    public Proposal fetch(String id) {
        JpaProposal jpaProposal = jpaProposalRepository.findByNaturalId(id).orElseThrow(EntityNotFoundException::new);
        Proposal proposal = Proposal.builder()
                .surrogateKey(jpaProposal.getSurrogateKey())
                .id(new Id(jpaProposal.getId()))
                .esal(new ESAL(jpaProposal.getEsal().getName(), new Id(jpaProposal.getEsal().getId())))
                .title(jpaProposal.getTitle())
                .location(new Location(
                        jpaProposal.getLocation().getProvince(),
                        jpaProposal.getLocation().getTown(),
                        jpaProposal.getLocation().getAddress()))
                .expirationDate(jpaProposal.getExpirationDate())
                .minimumAge(jpaProposal.getMinimumAge())
                .maximumAge(jpaProposal.getMaximumAge())
                .requiredDays(jpaProposal.getRequiredDays())
                .published(jpaProposal.getPublished())
                .description(jpaProposal.getDescription())
                .durationInDays(jpaProposal.getDurationInDays())
                .startingDate(jpaProposal.getStartingDate())
                .category(ProposalCategory.valueOf(jpaProposal.getCategory()))
                .build();
        jpaProposal.getInscribedVolunteers()
                .stream()
                .map(v -> new Volunteer(EmailAddress.from(v.getCredential().getEmail()), new Id(v.getId())))
                .forEach(proposal::inscribeVolunteer);
        return proposal;
    }
}
