package com.huellapositiva.integration;

import com.huellapositiva.application.dto.AuthenticationRequestDto;
import com.huellapositiva.domain.actions.RegisterVolunteerAction;
import com.huellapositiva.infrastructure.EmailService;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestData.class)
class RegisterVolunteerActionShould {

    @Autowired
    private RegisterVolunteerAction registerVolunteerAction;

    @Autowired
    private TestData testData;

    @MockBean
    private EmailService emailService;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    void registering_a_volunteer_should_send_email_to_confirm_email_address(){
        //GIVEN
        AuthenticationRequestDto dto = new AuthenticationRequestDto("foo@huellapositiva.com", "plain-password");

        //WHEN
        registerVolunteerAction.execute(dto);

        //THEN
        verify(emailService, times(1)).sendEmail(any());
    }
}

