package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.ProposalRevisionDto;
import com.huellapositiva.domain.model.valueobjects.ProposalRevisionEmail;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.domain.service.ProposalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;

@RequiredArgsConstructor
@Service
public class SubmitProposalRevisionAction {

    private final EmailCommunicationService communicationService;

    private final ProposalService proposalService;

    /**
     * Fetch the ESAL of the database for get the ContactPerson, change status of the proposal and send an email with the revision of the reviser.
     *
     * @param proposalId : The id of the proposal to be revised.
     * @param revisionDto : Contains the email reviser and the feedback if has it.
     * @param proposalURI : URI the proposal to revise.
     * @param accountId Account ID of logged user
     */
    public void execute(String proposalId, ProposalRevisionDto revisionDto, URI proposalURI, String accountId) {
        ProposalRevisionEmail revisionEmail = proposalService.requestChanges(proposalId, revisionDto, proposalURI, accountId);
        communicationService.sendSubmittedProposalRevision(revisionEmail);
    }
}
