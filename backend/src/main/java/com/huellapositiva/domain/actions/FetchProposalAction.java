package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.ProposalResponseDto;
import com.huellapositiva.application.exception.ProposalNotPublished;
import com.huellapositiva.domain.repository.ProposalRepository;
import com.huellapositiva.infrastructure.orm.entities.JpaProposal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class FetchProposalAction {

    private final ProposalRepository proposalRepository;

    public ProposalResponseDto execute(String proposalId) {
        JpaProposal proposal = proposalRepository.fetch(proposalId);
        boolean isNotPublished = !proposal.getPublished();
        if (isNotPublished) {
            throw new ProposalNotPublished();
        }
        return ProposalResponseDto.builder()
                .title(proposal.getTitle())
                .esalName(proposal.getEsal().getName())
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
