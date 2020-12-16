package com.huellapositiva.domain.repository;

import com.huellapositiva.application.exception.ESALNotFoundException;
import com.huellapositiva.domain.exception.InvalidStatusIdException;
import com.huellapositiva.domain.model.entities.Proposal;
import com.huellapositiva.domain.model.entities.Volunteer;
import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.domain.model.valueobjects.Requirement;
import com.huellapositiva.domain.model.valueobjects.Skill;
import com.huellapositiva.infrastructure.orm.entities.*;
import com.huellapositiva.infrastructure.orm.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.huellapositiva.domain.model.valueobjects.ProposalStatus.INADEQUATE;
import static com.huellapositiva.domain.model.valueobjects.ProposalStatus.PUBLISHED;

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

    @Autowired
    private final JpaProposalStatusRepository jpaProposalStatusRepository;

    @Autowired
    private final JpaVolunteersProposalsRepository jpaVolunteersProposalsRepository;

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
                .zipCode(proposal.getLocation().getZipCode())
                .island(proposal.getLocation().getIsland())
                .build());
        JpaESAL esal = jpaESALRepository.findByName(proposal.getEsal().getName())
                .orElseThrow(ESALNotFoundException::new);
        Set<JpaVolunteer> volunteers = proposal.getInscribedVolunteers()
                .stream()
                .map(v -> jpaVolunteerRepository.findByIdWithCredentialsAndRoles(v.getId().toString()).get())
                .collect(Collectors.toSet());
        JpaProposalStatus jpaProposalStatus = jpaProposalStatusRepository.findById(proposal.getStatus().getId())
                .orElseThrow(InvalidStatusIdException::new);
        JpaProposal jpaProposal = JpaProposal.builder()
                .id(proposal.getId().toString())
                .title(proposal.getTitle())
                .esal(esal)
                .location(jpaLocation)
                .startingProposalDate(proposal.getStartingProposalDate().getDate())
                .closingProposalDate(proposal.getClosingProposalDate().getDate())
                .startingVolunteeringDate(proposal.getStartingVolunteeringDate().getDate())
                .requiredDays(proposal.getRequiredDays())
                .minimumAge(proposal.getPermittedAgeRange().getMinimum())
                .maximumAge(proposal.getPermittedAgeRange().getMaximum())
                .status(jpaProposalStatus)
                .description(proposal.getDescription())
                .durationInDays(proposal.getDurationInDays())
                .category(proposal.getCategory().toString())
                .inscribedVolunteers(volunteers)
                .extraInfo(proposal.getExtraInfo())
                .instructions(proposal.getInstructions())
                .imageUrl(proposal.getImage() != null ? proposal.getImage().toExternalForm() : null)
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

    @SneakyThrows
    public Proposal fetch(String id) {
        JpaProposal jpaProposal = jpaProposalRepository.findByNaturalId(id)
                .orElseThrow(EntityNotFoundException::new);
        Proposal proposal = Proposal.parseJpa(jpaProposal);
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

    public List<Proposal> fetchAllPublishedPaginated(int page, int size) {
        Sort sortByClosingDateProximity = Sort.by("closingProposalDate");
        JpaProposalStatus statusPublished = jpaProposalStatusRepository.findById(PUBLISHED.getId())
                .orElseThrow(() -> new RuntimeException("Status PUBLISHED not found."));
        return jpaProposalRepository.findByStatusIs(statusPublished, PageRequest.of(page, size, sortByClosingDateProximity))
            .stream()
            .map(Proposal::parseJpa)
            .collect(Collectors.toList());
    }

    public List<Proposal> fetchAllPaginated(int page, int size) {
        Sort sortByClosingDateProximity = Sort.by("closingProposalDate");
        JpaProposalStatus statusInadequate = jpaProposalStatusRepository.findById(INADEQUATE.getId())
                .orElseThrow(() -> new RuntimeException("Status INADEQUATE not found."));
        return jpaProposalRepository.findByStatusNot(statusInadequate, PageRequest.of(page, size, sortByClosingDateProximity))
                .stream()
                .map(Proposal::parseJpa)
                .collect(Collectors.toList());
    }

    public Integer updateProposalStatus (String id, JpaProposalStatus status) {
        return jpaProposalRepository.updateStatusById(id, status);
    }
}
