package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.ProposalResponseDto;
import com.huellapositiva.application.dto.SkillDto;
import com.huellapositiva.application.dto.VolunteerDto;
import com.huellapositiva.application.exception.ProposalNotPublicException;
import com.huellapositiva.application.exception.ProposalNotPublishedException;
import com.huellapositiva.domain.model.entities.Proposal;
import com.huellapositiva.domain.model.valueobjects.Requirement;
import com.huellapositiva.domain.repository.ProposalRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaVolunteersProposalsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

import static com.huellapositiva.domain.model.valueobjects.ProposalStatus.FINISHED;
import static com.huellapositiva.domain.model.valueobjects.ProposalStatus.PUBLISHED;

@RequiredArgsConstructor
@Service
public class FetchProposalAction {

    private final ProposalRepository proposalRepository;

    @Autowired
    JpaVolunteersProposalsRepository volunteersProposals;


    /**
     * This method fetches a proposal based on its id
     *
     * @param proposalId passed as path variable parameter
     * @return ProposalResponseDto
     * @throws ProposalNotPublishedException if the proposal is not PUBLISHED
     */
    public ProposalResponseDto execute(String proposalId) {
        Proposal proposal = proposalRepository.fetch(proposalId);
        if (proposal.getStatus() != PUBLISHED && proposal.getStatus() != FINISHED) {
            throw new ProposalNotPublicException("Proposal not public. Proposal ID: " + proposalId);
        }
        return ProposalResponseDto.builder()
                .id(proposal.getId().getValue())
                .title(proposal.getTitle())
                .esalName(proposal.getEsal().getName())
                .province(proposal.getLocation().getProvince())
                .town(proposal.getLocation().getTown())
                .address(proposal.getLocation().getAddress())
                .startingProposalDate(proposal.getStartingProposalDate().toString())
                .closingProposalDate(proposal.getClosingProposalDate().toString())
                .startingVolunteeringDate(proposal.getStartingVolunteeringDate().toString())
                .maximumAge(proposal.getPermittedAgeRange().getMinimum())
                .minimumAge(proposal.getPermittedAgeRange().getMaximum())
                .requiredDays(proposal.getRequiredDays())
                .status(proposal.getStatus().getId())
                .instructions(proposal.getInstructions())
                .extraInfo(proposal.getExtraInfo())
                .imageURL(proposal.getImage().toExternalForm())
                .skills(proposal.getSkills()
                        .stream()
                        .map(s -> new SkillDto(s.getName(), s.getDescription()))
                        .collect(Collectors.toList()))
                .requirements(proposal.getRequirements()
                        .stream()
                        .map(Requirement::getName)
                        .collect(Collectors.toList()))
                .inscribedVolunteers(proposal.getInscribedVolunteers()
                        .stream()
                        .map(v -> new VolunteerDto(v.getId().toString(), v.getEmailAddress().toString(),
                                volunteersProposals.findByIdOfProposalAndVolunteer(
                                        v.getId().toString(),
                                        proposal.getId().toString()).isConfirmed()))
                        .collect(Collectors.toList()))
                .build();
    }
}
