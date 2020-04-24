package com.huellapositiva.unit;

import com.huellapositiva.application.dto.RegisterVolunteerRequestDto;
import com.huellapositiva.domain.actions.RegisterVolunteerAction;
import com.huellapositiva.domain.exception.TemplateNotAvailableException;
import com.huellapositiva.domain.service.VolunteerService;
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
public class RegisterVolunteerActionShould {
    @Mock
    TemplateService templateService;
    @Mock
    VolunteerService volunterService;
    @Mock
    EmailService emailService;
    @Test
    void send_confirmation_email_even_when_template_service_fails() {
        //GIVEN
        RegisterVolunteerAction registerVolunteerAction = new RegisterVolunteerAction(volunterService,emailService,templateService);
        when(templateService.getEmailConfirmationTemplate()).thenThrow(TemplateNotAvailableException.class);
        //WHEN
       registerVolunteerAction.execute(RegisterVolunteerRequestDto.builder()
               .email("foo@huellapositiva.com")
               .password("123456")
               .build());
        //THEN
        verify(emailService).sendEmail(any());
    }
}
