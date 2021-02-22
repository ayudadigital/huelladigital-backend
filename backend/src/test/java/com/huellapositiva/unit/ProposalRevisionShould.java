package com.huellapositiva.unit;

import com.huellapositiva.application.dto.ProposalRevisionDto;
import com.huellapositiva.domain.actions.SubmitProposalRevisionAction;
import com.huellapositiva.domain.model.entities.ContactPerson;
import com.huellapositiva.domain.model.entities.Reviser;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.domain.model.valueobjects.ProposalRevisionEmail;
import com.huellapositiva.domain.model.valueobjects.Token;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.domain.service.ProposalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProposalRevisionShould {

    private SubmitProposalRevisionAction submitProposalRevisionAction;

    private static final String PATH_ID = "/{id}";

    @Mock
    private EmailCommunicationService emailCommunicationService;

    @Mock
    private ProposalService proposalService;

    @Mock
    private ContactPerson contactPerson;

    @Mock
    private Reviser reviser;

    @BeforeEach
    void beforeEach(){
        submitProposalRevisionAction = new SubmitProposalRevisionAction(emailCommunicationService, proposalService);
    }

    @Test
    void send_email_when_reviser_does_revision_of_the_proposal() {
        String proposalID = "00000000-0000-0000-0000-000000000000";
        ProposalRevisionDto proposalRevisionDto = ProposalRevisionDto
                .builder()
                .feedback("Cambia el nombre")
                .build();
        URI uri = ServletUriComponentsBuilder
                .fromUriString("/api/vi/proposals/revision/" + proposalID + "/" + proposalID)
                .build().toUri();
        String accountID = "00000000-0000-0000-0000-000000000000";

        ProposalRevisionEmail proposalRevisionEmail = ProposalRevisionEmail.builder()
                .proposalId(new Id(proposalID))
                .proposalURI(uri)
                .feedback(proposalRevisionDto.getFeedback())
                .esalContactPerson(contactPerson)
                .reviser(reviser)
                .token(Token.createToken())
                .build();

        when(proposalService.requestChanges(proposalID, proposalRevisionDto, uri, accountID))
                .thenReturn(proposalRevisionEmail);

        submitProposalRevisionAction.execute(proposalID, proposalRevisionDto, uri, accountID);

        verify(emailCommunicationService).sendSubmittedProposalRevision(proposalRevisionEmail);
    }
}
