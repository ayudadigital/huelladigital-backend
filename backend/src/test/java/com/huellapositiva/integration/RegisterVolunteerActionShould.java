package com.huellapositiva.integration;

import com.huellapositiva.application.dto.RegisterVolunteerRequestDto;
import com.huellapositiva.domain.Email;
import com.huellapositiva.domain.actions.RegisterVolunteerAction;
import com.huellapositiva.infrastructure.EmailService;
import com.huellapositiva.infrastructure.orm.model.FailEmailConfirmation;
import com.huellapositiva.infrastructure.orm.repository.JpaFailEmailConfirmationRepository;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestData.class)
class RegisterVolunteerActionShould {

    @Autowired
    private RegisterVolunteerAction registerVolunteerAction;

    @Autowired
    private JpaFailEmailConfirmationRepository failEmailConfirmationRepository;

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
        RegisterVolunteerRequestDto dto = new RegisterVolunteerRequestDto("foo@huellapositiva.com", "plain-password");

        //WHEN
        registerVolunteerAction.execute(dto);

        //THEN
        verify(emailService, times(1)).sendEmail(any());
    }

    @Test
    void registering_a_volunteer_failure_send_email_confirmation_should_save_email_address_in_db(){
        //GIVEN
        RegisterVolunteerRequestDto dto = new RegisterVolunteerRequestDto("foo@huellapositiva.com", "plain-password");
        Email email = Email.builder()
                .from("noreply@huellapositiva.com")
                .to(dto.getEmail())
                .subject("test")
                .body("test")
                .build();

        //WHEN
        registerVolunteerAction.execute(dto);
//        doThrow(new Exception()).when(emailService).sendEmail(email);
//        doThrow(new Exception()).when(registerVolunteerAction).execute(dto);
//        when(registerVolunteerAction.execute(dto)).thenThrow(new Exception());

        //THEN
        FailEmailConfirmation failEmailConfirmation = failEmailConfirmationRepository.findByEmail(email.getTo()).get();
        assertThat(failEmailConfirmation.getEmail(), is(email.getTo()));
    }
}

