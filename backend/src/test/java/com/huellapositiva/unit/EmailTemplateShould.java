package com.huellapositiva.unit;

import com.huellapositiva.domain.exception.EmptyTemplateException;
import com.huellapositiva.domain.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.valueobjects.EmailTemplate;
import com.huellapositiva.infrastructure.TemplateService;
import org.junit.jupiter.api.Test;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.*;

/*TO DO
* llega la plantilla, parsea la plantilla
* no se encuentra la plantilla
* se encuentra pero no se puede parsear
 */

class EmailTemplateShould {


    @Test
    void throw_an_exception_if_receive_a_empty_text(){
        //GIVEN

        //WHEN

        //THEN
        assertThrows(EmptyTemplateException.class,()->EmailTemplate.createEmailTemplate(""));
    }

    @Test
    void receive_a_template_and_parse_it(){
        //GIVEN
        TemplateService templateService = new TemplateService();
        EmailTemplate emailTemplate = templateService.getEmailConfirmationTemplate();
        EmailConfirmation emailConfirmation = EmailConfirmation.from("foo@fuu.com");
        //WHEN
        emailTemplate.parse(emailConfirmation);
        //THEN
        String template = "Te acabas de registrar en huellapositiva.com\n" +
                "Para confirmar tu correo electrónico, haz clic en el enlace\n" +
                "<a href=\""+ emailConfirmation.getToken() +"\">Clic aquí</a>";

        assertThat(emailTemplate.getParsedTemplate(), is (template));
    }
}