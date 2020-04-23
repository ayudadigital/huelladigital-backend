package com.huellapositiva.unit;

import com.huellapositiva.domain.Email;
import com.huellapositiva.domain.exception.EmailNotValidException;
import com.huellapositiva.domain.exception.EmptyTemplateException;
import com.huellapositiva.domain.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.valueobjects.EmailTemplate;
import com.huellapositiva.infrastructure.TemplateService;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.*;

class EmailShould {

    @Test
    void not_allow_empty_fields() {
        //GIVEN
        EmailConfirmation emailConfirmation = EmailConfirmation.from("");
        EmailTemplate emailTemplate = EmailTemplate.createEmailTemplate("template-body");
        //WHEN +THEN
        assertThrows(EmailNotValidException.class,()-> Email.createFrom(emailConfirmation, emailTemplate));
    }

}

//        assertThat(emailTemplate.getParsedTemplate(), is (template));


//                .builder()
//                .from("noreply@huellapositiva.com")
//                .to(dto.getEmail())
//                .subject("Confirmación de la cuenta en huellapositiva")
//                .body("Bienvenido a huella positiva, por favor confirme su email haciendo click aquí")
//                .build();