package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.ListedProposalsDto;
import com.huellapositiva.application.dto.ProposalLiteDto;
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
                    ProposalLiteDto.builder()
                            .id(proposal.getId().toString())
                            .title(proposal.getTitle())
                            .province(proposal.getLocation().getProvince())
                            .town(proposal.getLocation().getTown())
                            .address(proposal.getLocation().getAddress())
                            .closingProposalDate(proposal.getClosingProposalDate().toString())
                            .startingVolunteeringDate(proposal.getStartingVolunteeringDate().toString())
                            .maximumAge(proposal.getPermittedAgeRange().getMinimum())
                            .minimumAge(proposal.getPermittedAgeRange().getMaximum())
                            .published(proposal.isPublished())
                            .duration(proposal.getDurationInDays())
                            .imageURL(proposal.getImage().toExternalForm())
                            .build())
             .collect(Collectors.toList())
        );
    }
}
