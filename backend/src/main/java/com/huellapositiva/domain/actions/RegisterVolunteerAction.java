package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.RegisterVolunteerRequestDto;
import com.huellapositiva.domain.EmailBuilder;
import com.huellapositiva.domain.service.VolunteerService;
import com.huellapositiva.domain.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.valueobjects.PlainPassword;
import com.huellapositiva.infrastructure.EmailService;
import com.huellapositiva.domain.PhysicalEmail;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RegisterVolunteerAction {

    private final VolunteerService volunteerService;

    private final EmailService emailService;

    public void execute(RegisterVolunteerRequestDto dto) {
        EmailConfirmation emailConfirmation = EmailConfirmation.from(dto.getEmail());
        volunteerService.registerVolunteer(PlainPassword.from(dto.getPassword()), emailConfirmation);
        PhysicalEmail physicalEmail = EmailBuilder.createEmailAddressConfirmation(emailConfirmation);
//                .builder()
//                .from("noreply@huellapositiva.com")
//                .to(dto.getEmail())
//                .subject("Confirmación de la cuenta en huellapositiva")
//                .body("Bienvenido a huella positiva, por favor confirme su email haciendo click aquí")
//                .build();
        emailService.sendEmail(physicalEmail);
    }
}