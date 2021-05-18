package com.huellapositiva.unit;

import com.huellapositiva.application.dto.RegisterContactPersonDto;
import com.huellapositiva.domain.actions.RegisterESALContactPersonAction;
import com.huellapositiva.domain.service.ESALContactPersonService;
import com.huellapositiva.domain.service.EmailCommunicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.huellapositiva.util.TestData.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RegisterContactPersonShould {

    @Mock
    private ESALContactPersonService esalContactPersonService;

    @Mock
    private EmailCommunicationService communicationService;

    private RegisterESALContactPersonAction registerESALContactPersonAction;

    @BeforeEach
    void beforeEach() {
        registerESALContactPersonAction = new RegisterESALContactPersonAction(esalContactPersonService, communicationService);
    }

    @Test
    void send_confirmation_email() {
        registerESALContactPersonAction.execute(RegisterContactPersonDto.builder()
                .name(VALID_NAME)
                .surname(VALID_SURNAME)
                .phoneNumber(VALID_PHONE)
                .email(DEFAULT_EMAIL)
                .password(DEFAULT_PASSWORD)
                .build());

        verify(communicationService).sendRegistrationConfirmationEmail(any());
    }
}
