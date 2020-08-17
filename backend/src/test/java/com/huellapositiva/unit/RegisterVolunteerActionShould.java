package com.huellapositiva.unit;

import com.huellapositiva.application.dto.AuthenticationRequestDto;
import com.huellapositiva.domain.actions.RegisterVolunteerAction;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.domain.service.VolunteerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RegisterVolunteerActionShould {

    @Mock
    VolunteerService volunteerService;

    @Mock
    EmailCommunicationService communicationService;

    private RegisterVolunteerAction registerVolunteerAction;

    @BeforeEach
    void beforeEach() {
        registerVolunteerAction = new RegisterVolunteerAction(volunteerService, communicationService);
    }

    @Test
    void send_confirmation_email() {
        registerVolunteerAction.execute(AuthenticationRequestDto.builder()
                .email("foo@huellapositiva.com")
                .password("123456")
                .build());

        verify(communicationService).sendRegistrationConfirmationEmail(any());
    }
}
