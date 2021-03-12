package com.huellapositiva.unit;

import com.huellapositiva.domain.actions.PublishProposalAction;
import com.huellapositiva.domain.dto.ChangeStatusToPublishedResult;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.domain.service.ProposalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.huellapositiva.util.TestData.DEFAULT_ESAL_CONTACT_PERSON_EMAIL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublishProposalActionShould {
    private PublishProposalAction publishProposalAction;

    @Mock
    private EmailCommunicationService emailCommunicationService;

    @Mock
    private ProposalService proposalService;

    @BeforeEach
    void beforeEach(){
        publishProposalAction = new PublishProposalAction(proposalService, emailCommunicationService);
    }

    @Test
    void send_change_email() {
        String proposalId = "1";
        when(proposalService.changeStatusToPublished(proposalId)).thenReturn(new ChangeStatusToPublishedResult(DEFAULT_ESAL_CONTACT_PERSON_EMAIL,"Recogida de ropita"));

        publishProposalAction.executeAsReviser(proposalId);

        verify(emailCommunicationService).sendMessageProposalPublished(any(), any());
    }
}
