package com.huellapositiva.integration;

import com.huellapositiva.domain.actions.RequestProposalRevisionAction;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestData.class)
class ProposalRevisionEmailRequestEmailServiceShould {

    @Autowired
    private RequestProposalRevisionAction requestProposalRevisionAction;

    @MockBean
    private EmailCommunicationService communicationService;

    @Test
    void send_an_email_to_the_reviser_when_a_new_proposal_is_created() {
        // GIVEN
        URI uri = UriComponentsBuilder.newInstance()
                .path("/{id}").buildAndExpand("id")
                .toUri();

        // WHEN
        requestProposalRevisionAction.execute(uri);

        // THEN
        verify(communicationService).sendRevisionRequestEmail(any());
    }
}
