package com.huellapositiva.unit;

import com.huellapositiva.domain.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.valueobjects.EmailTemplate;
import com.huellapositiva.infrastructure.TemplateService;
import org.junit.jupiter.api.Test;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class EmailTemplateShould {

    @Test
    void receive_a_template_and_parse_it() {
        TemplateService templateService = new TemplateService();
        EmailTemplate emailTemplate = templateService.getEmailConfirmationTemplate();
        EmailConfirmation emailConfirmation = EmailConfirmation.from("foo@fuu.com");

        emailTemplate.parse(emailConfirmation);

        String template = "Te acabas de registrar en huellapositiva.com\n" +
                "Para confirmar tu correo electrónico, haz clic en el enlace\n" +
                "<a href=\"" + emailConfirmation.getToken() + "\">Clic aquí</a>";
        assertThat(emailTemplate.getParsedTemplate(), is(template));
    }
}