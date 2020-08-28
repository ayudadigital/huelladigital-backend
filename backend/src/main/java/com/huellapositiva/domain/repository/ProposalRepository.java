package com.huellapositiva.domain.repository;

import com.huellapositiva.application.exception.ESALNotFound;
import com.huellapositiva.domain.model.entities.ESAL;
import com.huellapositiva.domain.model.entities.Proposal;
import com.huellapositiva.domain.model.entities.Volunteer;
import com.huellapositiva.domain.model.valueobjects.*;
import com.huellapositiva.infrastructure.orm.entities.JpaESAL;
import com.huellapositiva.infrastructure.orm.entities.JpaLocation;
import com.huellapositiva.infrastructure.orm.entities.JpaProposal;
import com.huellapositiva.infrastructure.orm.entities.JpaVolunteer;
import com.huellapositiva.infrastructure.orm.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
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

    @Autowired
    private final JpaProposalSkillsRepository jpaProposalSkillsRepository;

    @Autowired
    private final JpaProposalRequirementsRepository jpaProposalRequirementsRepository;

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
                .expirationDate(proposal.getExpirationDate().getDate())
                .requiredDays(proposal.getRequiredDays())
                .minimumAge(proposal.getPermitedAgeRange().getMinimum())
                .maximumAge(proposal.getPermitedAgeRange().getMaximum())
                .published(proposal.isPublished())
                .description(proposal.getDescription())
                .durationInDays(proposal.getDurationInDays())
                .startingDate(proposal.getStartingDate().getDate())
                .category(proposal.getCategory().toString())
                .inscribedVolunteers(volunteers)
                .extraInfo(proposal.getExtraInfo())
                .instructions(proposal.getInstructions())
                .imageUrl(proposal.getImage().toExternalForm())
                .build();
        if (proposal.getSurrogateKey() != null) {
            jpaProposal.setSurrogateKey(proposal.getSurrogateKey());
        }
        jpaProposalRepository.save(jpaProposal);
        proposal.getSkills()
                .forEach(skill -> jpaProposalSkillsRepository.insert(skill.getName(), skill.getDescription(), proposal.getId().toString()));
        proposal.getRequirements()
                .forEach(requirement -> jpaProposalRequirementsRepository.insert(requirement.getName(), proposal.getId().toString()));
        return proposal.getId().toString();
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
                .expirationDate(new ProposalDate(jpaProposal.getExpirationDate()))
                .permitedAgeRange(AgeRange.create(jpaProposal.getMinimumAge(), jpaProposal.getMaximumAge()))
                .requiredDays(jpaProposal.getRequiredDays())
                .published(jpaProposal.getPublished())
                .description(jpaProposal.getDescription())
                .durationInDays(jpaProposal.getDurationInDays())
                .startingDate(new ProposalDate(jpaProposal.getStartingDate()))
                .category(ProposalCategory.valueOf(jpaProposal.getCategory()))
                .extraInfo(jpaProposal.getExtraInfo())
                .instructions(jpaProposal.getInstructions())
                .build();
        if (jpaProposal.getImageUrl() != null) {
            try {
                proposal.setImage(new URL(jpaProposal.getImageUrl()));
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }
        }
        jpaProposal.getInscribedVolunteers()
                .stream()
                .map(v -> new Volunteer(EmailAddress.from(v.getCredential().getEmail()), new Id(v.getId())))
                .forEach(proposal::inscribeVolunteer);
        jpaProposal.getSkills()
                .forEach(s -> proposal.addSkill(new Skill(s.getName(), s.getDescription())));
        jpaProposal.getRequirements()
                .forEach(r -> proposal.addRequirement(new Requirement(r.getName())));

        return proposal;
    }
}
