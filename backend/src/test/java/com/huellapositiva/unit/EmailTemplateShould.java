package com.huellapositiva.unit;

import com.huellapositiva.domain.exception.EmailNotValidException;
import com.huellapositiva.domain.model.entities.ContactPerson;
import com.huellapositiva.domain.model.entities.Reviser;
import com.huellapositiva.domain.model.entities.User;
import com.huellapositiva.domain.model.valueobjects.*;
import com.huellapositiva.infrastructure.TemplateService;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static com.huellapositiva.util.TestData.DEFAULT_EMAIL;
import static com.huellapositiva.util.TestData.DEFAULT_ESAL_CONTACT_PERSON_EMAIL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmailTemplateShould {


    private final String baseUrl = "https://plataforma.huellapositiva.com/api/v1/email-confirmation/";

    @Test
    void receive_a_template_and_parse_it() {
        // GIVEN
        TemplateService templateService = new TemplateService();
        EmailConfirmation emailConfirmation = EmailConfirmation.from("foo@fuu.com", baseUrl);
        EmailTemplate emailTemplate = templateService.getEmailConfirmationTemplate(emailConfirmation);
        Map<String, String> variables = new HashMap<>();
        variables.put("CONFIRMATION_URL", emailConfirmation.getUrl() );

        // WHEN
        EmailTemplate result = emailTemplate.parse(variables);

        // THEN
        assertThat(result.getParsedTemplate().contains(emailConfirmation.getUrl()), is(true));
    }

    @Test
    void fail_when_cannot_replace_all_variables() {
        // GIVEN
        TemplateService templateService = new TemplateService();
        EmailConfirmation emailConfirmation = EmailConfirmation.from(DEFAULT_EMAIL, baseUrl);
        EmailTemplate emailTemplate = templateService.getEmailConfirmationTemplate(emailConfirmation);
        Map<String, String> variables = new HashMap<>();

        // WHEN + THEN
        assertThrows(EmailNotValidException.class, () -> emailTemplate.parse(variables));
    }

    @Test
    void parse_with_generic_values_when_a_provided_name_is_null() throws URISyntaxException {
        TemplateService templateService = new TemplateService();
        ProposalRevisionEmail proposalRevisionEmail = ProposalRevisionEmail.builder()
                .reviser(Reviser.from(new User(Id.newId(), EmailAddress.from(DEFAULT_EMAIL), Id.newId())))
                .esalContactPerson(new ContactPerson(Id.newId(), EmailAddress.from(DEFAULT_ESAL_CONTACT_PERSON_EMAIL), Id.newId()))
                .proposalURI(new URI("https://www.dummyURL.org/"))
                .feedback("This is an example of a revision overview")
                .proposalId(Id.newId())
                .token(Token.createToken())
                .build();
        EmailTemplate result = templateService.getProposalRevisionWithFeedbackTemplate(proposalRevisionEmail);

        assertThat(result.getParsedTemplate().contains("null"), is(false));
    }
}