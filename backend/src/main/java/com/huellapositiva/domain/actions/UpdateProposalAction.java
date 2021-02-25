package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.UpdateProposalRequestDto;
import com.huellapositiva.domain.model.entities.Proposal;
import com.huellapositiva.domain.model.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.model.valueobjects.ProposalRevisionRequestEmail;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.domain.service.ProposalService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.text.ParseException;

@Service
@RequiredArgsConstructor
public class UpdateProposalAction {

    private final ProposalService proposalService;

    private final EmailCommunicationService emailCommunicationService;

    @Value("${huellapositiva.revision.email.from}")
    private String reviserEmail;

    public void execute(UpdateProposalRequestDto updateProposalRequestDto, String accountId) throws ParseException {
        proposalService.updateProposal(updateProposalRequestDto, accountId);

        String uri = "http://localhost/api/v1/proposals/" + updateProposalRequestDto.getId();
        ProposalRevisionRequestEmail email = ProposalRevisionRequestEmail.from(reviserEmail, uri);
        emailCommunicationService.sendMessageUpdateProposal(email);
    }

}
