package com.huellapositiva.unit;

import com.huellapositiva.application.dto.ChangeReviewPendingProposalToPublishedDto;
import com.huellapositiva.domain.actions.ChangeReviewPendingProposalToPublishedAction;
import com.huellapositiva.domain.dto.ChangeStatusToPublishedResult;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.domain.service.ProposalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static com.huellapositiva.util.TestData.*;

@ExtendWith(MockitoExtension.class)
class ChangeReviewPendingProposalToPublishedActionShould {
    private ChangeReviewPendingProposalToPublishedAction changeReviewPendingProposalToPublishedAction;

    @Mock
    private EmailCommunicationService emailCommunicationService;

    @Mock
    private ProposalService proposalService;

    @BeforeEach
    void beforeEach(){
        changeReviewPendingProposalToPublishedAction = new ChangeReviewPendingProposalToPublishedAction(proposalService, emailCommunicationService);
    }

    @Test
    void send_change_email() {
        ChangeReviewPendingProposalToPublishedDto dto = ChangeReviewPendingProposalToPublishedDto.builder().idProposal("1").build();

        when(proposalService.changeStatusToPublished(dto.getIdProposal())).thenReturn(new ChangeStatusToPublishedResult(DEFAULT_ESAL_CONTACT_PERSON_EMAIL,"Recogida de ropita"));

        changeReviewPendingProposalToPublishedAction.execute(dto.getIdProposal());

        verify(emailCommunicationService).sendMessageProposalPublished(any(), any());
    }
}
