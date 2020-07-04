package com.huellapositiva.integration;

import com.huellapositiva.domain.actions.ResendEmailConfirmationAction;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.infrastructure.orm.model.EmailConfirmation;
import com.huellapositiva.infrastructure.orm.repository.JpaEmailConfirmationRepository;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;
import java.util.UUID;

import static com.huellapositiva.util.TestData.DEFAULT_EMAIL;
import static com.huellapositiva.util.TestData.DEFAULT_PASSWORD;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestData.class)
public class ResendEmailConfirmationActionShould {

    @Autowired
    private TestData testData;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private ResendEmailConfirmationAction resendEmailConfirmationAction;

    @Autowired
    private JpaEmailConfirmationRepository jpaEmailConfirmationRepository;

    @MockBean
    private EmailCommunicationService communicationService;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    void update_hash_and_resend_email() {
        //GIVEN
        UUID initialHash = UUID.randomUUID();
        testData.createCredential(DEFAULT_EMAIL, DEFAULT_PASSWORD, initialHash);

        //WHEN
        UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(DEFAULT_EMAIL, DEFAULT_PASSWORD, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authReq);
        resendEmailConfirmationAction.execute();

        //THEN
        EmailConfirmation newEmailConfirmation = jpaEmailConfirmationRepository.findByEmail(DEFAULT_EMAIL)
                .orElseThrow(() -> new UsernameNotFoundException("User with username: " + DEFAULT_EMAIL + " was not found."));
        String newHash = newEmailConfirmation.getHash();
        assertThat(initialHash, is(not(newHash)));
        verify(communicationService).sendRegistrationConfirmationEmail(any());
    }
}
