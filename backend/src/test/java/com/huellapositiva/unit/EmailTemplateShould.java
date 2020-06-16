package com.huellapositiva.unit;

import com.huellapositiva.domain.exception.EmailNotValidException;
import com.huellapositiva.domain.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.valueobjects.EmailTemplate;
import com.huellapositiva.infrastructure.TemplateService;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

import static com.huellapositiva.util.TestData.DEFAULT_EMAIL;
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

        // WHEN THEN
        assertThrows(EmailNotValidException.class, () -> emailTemplate.parse(variables));
    }


}