package com.huellapositiva.unit;

import com.huellapositiva.domain.model.valueobjects.EmailMessage;
import com.huellapositiva.domain.exception.EmailNotValidException;
import com.huellapositiva.domain.model.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.model.valueobjects.EmailTemplate;
import org.junit.jupiter.api.Test;

import static com.huellapositiva.util.TestData.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.*;

class EmailMessageShould {

    @Test
    void not_allow_empty_fields() {
        //GIVEN
        EmailConfirmation emailConfirmation = EmailConfirmation.from("", "");
        EmailTemplate emailTemplate = new EmailTemplate("template-body");
        String emailAddress = emailConfirmation.getEmailAddress();
        //WHEN +THEN
        assertThrows(EmailNotValidException.class, () -> EmailMessage.createFrom(DEFAULT_FROM, emailAddress, DEFAULT_SUBJECT, emailTemplate));
    }

    @Test
    void create_a_valid_email_message_object() {
        //GIVEN
        EmailTemplate emailTemplate = new EmailTemplate("template-body");
        //WHEN
        EmailMessage emailMessage = EmailMessage.createFrom(DEFAULT_FROM, DEFAULT_EMAIL, DEFAULT_SUBJECT, emailTemplate);
        //THEN
        assertThat(emailMessage.getFrom(), is(DEFAULT_FROM));
        assertThat(emailMessage.getTo(), is(DEFAULT_EMAIL));
        assertThat(emailMessage.getSubject(), is(DEFAULT_SUBJECT));
        assertThat(emailMessage.getBody(), is(emailTemplate.getParsedTemplate()));
    }
}