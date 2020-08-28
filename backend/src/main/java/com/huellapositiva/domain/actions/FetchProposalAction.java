package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.ProposalResponseDto;
import com.huellapositiva.application.exception.ProposalNotPublished;
import com.huellapositiva.domain.model.entities.Proposal;
import com.huellapositiva.domain.repository.ProposalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;

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
                .expirationDate(proposal.getExpirationDate().toString())
                .maximumAge(proposal.getPermitedAgeRange().getMinimum())
                .minimumAge(proposal.getPermitedAgeRange().getMaximum())
                .requiredDays(proposal.getRequiredDays())
                .published(proposal.isPublished())
                .build();
    }
}
