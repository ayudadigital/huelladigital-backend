package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.ProposalResponseDto;
import com.huellapositiva.application.dto.SkillDto;
import com.huellapositiva.application.dto.VolunteerDto;
import com.huellapositiva.application.exception.ProposalNotPublished;
import com.huellapositiva.domain.model.entities.Proposal;
import com.huellapositiva.domain.model.valueobjects.Requirement;
import com.huellapositiva.domain.repository.ProposalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class FetchProposalAction {

    private final ProposalRepository proposalRepository;

    public ProposalResponseDto execute(String proposalId) {
        Proposal proposal = proposalRepository.fetch(proposalId);
        boolean isNotPublished = !proposal.isPublished();
        if (isNotPublished) {
            throw new ProposalNotPublished();
        }
        return ProposalResponseDto.builder()
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
                .published(proposal.isPublished())
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
                        .map(v -> new VolunteerDto(v.getId().toString()))
                        .collect(Collectors.toList()))
                .build();
    }
}
