package com.huellapositiva.unit;

import com.huellapositiva.application.dto.CredentialsVolunteerRequestDto;
import com.huellapositiva.domain.actions.RegisterVolunteerAction;
import com.huellapositiva.domain.service.VolunteerService;
import com.huellapositiva.domain.valueobjects.EmailTemplate;
import com.huellapositiva.infrastructure.EmailService;
import com.huellapositiva.infrastructure.TemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterVolunteerActionShould {
    @Mock
    TemplateService templateService;
    @Mock
    VolunteerService volunteerService;
    @Mock
    EmailService emailService;

    private RegisterVolunteerAction registerVolunteerAction;

    @BeforeEach
    void beforeEach() {
        registerVolunteerAction = new RegisterVolunteerAction(
                volunteerService, emailService, templateService);
    }

    @Test
    void send_confirmation_email() {

        String template = "Te acabas de registrar en huellapositiva.com\n" +
                          "Para confirmar tu correo electrónico, haz clic en el enlace\n" +
                          "<a href=\"${CONFIRMATION_URL}\">Clic aquí</a>\n";
        lenient().when(templateService.getEmailConfirmationTemplate(any())).thenReturn(new EmailTemplate(template));

        registerVolunteerAction.execute(CredentialsVolunteerRequestDto.builder()
               .email("foo@huellapositiva.com")
               .password("123456")
               .build());

        verify(emailService).sendEmail(any());
    }
}
