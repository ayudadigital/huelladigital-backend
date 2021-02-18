package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.ChangeToInadequateDto;
import com.huellapositiva.domain.model.entities.Proposal;
import com.huellapositiva.domain.repository.ProposalRepository;
import com.huellapositiva.domain.service.EmailCommunicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static com.huellapositiva.domain.model.valueobjects.ProposalStatus.INADEQUATE;
import static com.huellapositiva.domain.model.valueobjects.ProposalStatus.REVIEW_PENDING;

@Service
@RequiredArgsConstructor
public class ChangeStatusToInadequateAction {

    @Autowired
    private ProposalRepository proposalRepository;

    @Autowired
    private EmailCommunicationService emailCommunicationService;

    public void execute(ChangeToInadequateDto dto) {
        Proposal proposal = proposalRepository.fetch(dto.getProposalId());
        if(proposal.getStatus() != REVIEW_PENDING)
            throw new IllegalStateException();
        proposal.setStatus(INADEQUATE);
        proposalRepository.save(proposal);
        emailCommunicationService.sendInadequateProposalEmail(proposal.getEsal().getContactPersonEmail(), proposal.getTitle(), dto.getReason());
    }
}
