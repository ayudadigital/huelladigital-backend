package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.UpdateProposalRequestDto;
import com.huellapositiva.domain.model.valueobjects.ProposalRevisionRequestEmail;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.domain.service.ProposalService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
public class UpdateProposalAction {

    private final ProposalService proposalService;

    private final EmailCommunicationService emailCommunicationService;

    @Value("${huellapositiva.revision.email.from}")
    private String reviserEmail;

    @Value("${huellapositiva.api.v1.base-url}")
    private String baseUrl;

    /**
     * This method execute the update the proposal and send an email with the URL with the proposal information
     *
     * @param proposalId Id of the proposal
     * @param updateProposalRequestDto The new information about the proposal
     * @param accountId The account id of the user
     */
    public void execute(String proposalId, UpdateProposalRequestDto updateProposalRequestDto, String accountId) throws ParseException {
        proposalService.updateProposal(proposalId, updateProposalRequestDto, accountId);

        String proposalUrl = format("%s/proposals/%s", baseUrl, proposalId);
        ProposalRevisionRequestEmail email = ProposalRevisionRequestEmail.from(reviserEmail, proposalUrl);
        emailCommunicationService.sendMessageUpdateProposal(email);
    }

}
