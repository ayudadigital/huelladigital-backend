package com.huellapositiva.unit;

import com.huellapositiva.application.dto.RegisterVolunteerRequestDto;
import com.huellapositiva.domain.actions.RegisterVolunteerAction;
import com.huellapositiva.domain.service.VolunteerService;
import com.huellapositiva.domain.valueobjects.EmailTemplate;
import com.huellapositiva.infrastructure.EmailService;
import com.huellapositiva.infrastructure.TemplateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterVolunteerActionShould {
    @Mock
    TemplateService templateService;
    @Mock
    VolunteerService volunteerService;
    @Mock
    EmailService emailService;
    @Test
    void send_confirmation_email() {
        RegisterVolunteerAction registerVolunteerAction = new RegisterVolunteerAction(volunteerService,emailService, templateService);
        String template = "Te acabas de registrar en huellapositiva.com\n" +
                          "Para confirmar tu correo electrónico, haz clic en el enlace\n" +
                          "<a href=\"${CONFIRMATION_URL}\">Clic aquí</a>\n";
        when(templateService.getEmailConfirmationTemplate(any())).thenReturn(new EmailTemplate(template));

        registerVolunteerAction.execute(RegisterVolunteerRequestDto.builder()
               .email("foo@huellapositiva.com")
               .password("123456")
               .build());

        verify(emailService).sendEmail(any());
    }
}
