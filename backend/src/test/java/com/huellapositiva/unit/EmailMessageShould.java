package com.huellapositiva.unit;

import com.huellapositiva.domain.model.valueobjects.EmailMessage;
import com.huellapositiva.domain.exception.EmailNotValidException;
import com.huellapositiva.domain.model.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.model.valueobjects.EmailTemplate;
import org.junit.jupiter.api.Test;

import static com.huellapositiva.util.TestData.DEFAULT_FROM;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.*;

class EmailMessageShould {

    @Test
    void not_allow_empty_fields() {
        //GIVEN
        EmailConfirmation emailConfirmation = EmailConfirmation.from("", "");
        EmailTemplate emailTemplate = new EmailTemplate("template-body");
        //WHEN +THEN
        assertThrows(EmailNotValidException.class, () -> EmailMessage.createFrom(emailConfirmation, emailTemplate, DEFAULT_FROM));
    }

    @Test
    void create_a_valid_email_object() {
        //GIVEN
        EmailConfirmation emailConfirmation = EmailConfirmation.from("foo@huellapositiva.com", "");
        EmailTemplate emailTemplate = new EmailTemplate("template-body");
        //WHEN
        EmailMessage emailMessage = EmailMessage.createFrom(emailConfirmation, emailTemplate, DEFAULT_FROM);
        //THEN
        assertThat(emailMessage.getFrom(), is(DEFAULT_FROM));
        assertThat(emailMessage.getTo(), is(emailConfirmation.getEmailAddress()));
        assertThat(emailMessage.getSubject(), is("Confirmación de la cuenta en huellapositiva"));
        assertThat(emailMessage.getBody(), is(emailTemplate.getParsedTemplate()));
    }

}