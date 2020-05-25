package com.huellapositiva.unit;

import com.huellapositiva.domain.Email;
import com.huellapositiva.domain.exception.EmailNotValidException;
import com.huellapositiva.domain.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.valueobjects.EmailTemplate;
import org.junit.jupiter.api.Test;

import static com.huellapositiva.util.TestData.DEFAULT_FROM;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.*;

class EmailShould {

    @Test
    void not_allow_empty_fields() {
        //GIVEN
        EmailConfirmation emailConfirmation = EmailConfirmation.from("", "");
        EmailTemplate emailTemplate = new EmailTemplate("template-body");
        //WHEN +THEN
        assertThrows(EmailNotValidException.class, () -> Email.createFrom(emailConfirmation, emailTemplate, DEFAULT_FROM));
    }

    @Test
    void create_a_valid_email_object() {
        //GIVEN
        EmailConfirmation emailConfirmation = EmailConfirmation.from("foo@huellapositiva.com", "");
        EmailTemplate emailTemplate = new EmailTemplate("template-body");
        //WHEN
        Email email = Email.createFrom(emailConfirmation, emailTemplate, DEFAULT_FROM);
        //THEN
        assertThat(email.getFrom(), is(DEFAULT_FROM));
        assertThat(email.getTo(), is(emailConfirmation.getEmailAddress()));
        assertThat(email.getSubject(), is("Confirmación de la cuenta en huellapositiva"));
        assertThat(email.getBody(), is(emailTemplate.getParsedTemplate()));
    }

}