package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.ListedProposalsDto;
import com.huellapositiva.application.dto.ProposalResponseDto;
import com.huellapositiva.domain.repository.ProposalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class FetchPaginatedProposalsAction {

    private final ProposalRepository proposalRepository;

    public ListedProposalsDto execute(int page, int size) {
        return new ListedProposalsDto(
            proposalRepository.fetchAllPaginated(page, size)
            .stream()
            .map(proposal ->
                    ProposalResponseDto.builder()
                            .id(proposal.getId().toString())
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
                            .inscribedVolunteersCount(proposal.getInscribedVolunteers().size())
                            .build())
             .collect(Collectors.toList())
        );
    }
}
