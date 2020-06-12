package com.huellapositiva.unit;

import com.huellapositiva.application.dto.CredentialsVolunteerRequestDto;
import com.huellapositiva.domain.actions.RegisterVolunteerAction;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.domain.service.VolunteerService;
import com.huellapositiva.infrastructure.security.JwtService;
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
    @Mock
    JwtService jwtService;

    private RegisterVolunteerAction registerVolunteerAction;

    @BeforeEach
    void beforeEach() {
        registerVolunteerAction = new RegisterVolunteerAction(
                volunteerService, communicationService, jwtService);
    }

    @Test
    void send_confirmation_email() {
        registerVolunteerAction.execute(CredentialsVolunteerRequestDto.builder()
                .email("foo@huellapositiva.com")
                .password("123456")
                .build());

        verify(communicationService).sendRegistrationConfirmationEmail(any());
    }

    @Test
    void authenticate_registered_volunteer() {
        registerVolunteerAction.authenticate(CredentialsVolunteerRequestDto.builder()
                .email("foo@huellapositiva.com")
                .password("123456")
                .build());
        verify(jwtService).create(any(), any());
    }
}
