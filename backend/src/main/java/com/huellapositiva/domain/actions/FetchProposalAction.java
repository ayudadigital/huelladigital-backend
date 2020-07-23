package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.ProposalResponseDto;
import com.huellapositiva.application.exception.ProposalNotPublished;
import com.huellapositiva.domain.service.ProposalService;
import com.huellapositiva.infrastructure.orm.entities.Proposal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class FetchProposalAction {

    private final ProposalService proposalService;

    public ProposalResponseDto execute(Integer proposalId) {
        Proposal proposal = proposalService.fetch(proposalId);
        boolean isNotPublished = !proposal.getPublished();
        if (isNotPublished) {
            throw new ProposalNotPublished();
        }
        return ProposalResponseDto.builder()
                .title(proposal.getTitle())
                .organization(proposal.getOrganization().getName())
                .province(proposal.getLocation().getProvince())
                .town(proposal.getLocation().getTown())
                .address(proposal.getLocation().getAddress())
                .expirationDate(proposal.getExpirationDate().toString())
                .maximumAge(proposal.getMaximumAge())
                .minimumAge(proposal.getMinimumAge())
                .requiredDays(proposal.getRequiredDays())
                .published(proposal.getPublished())
                .build();
    }
}
