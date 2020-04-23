package com.huellapositiva.infrastructure;

import com.huellapositiva.domain.valueobjects.EmailTemplate;
import org.springframework.stereotype.Service;

@Service
public class TemplateService {


    public EmailTemplate getEmailConfirmationTemplate() {
        return EmailTemplate.createEmailTemplate("Te acabas de registrar en huellapositiva.com\n" +
                "    Para confirmar tu correo electrónico, haz clic en el enlace\n" +
                "    <a href=\"${URL}\">Clic aquí</a>");
    }
}
