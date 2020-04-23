package com.huellapositiva.domain.valueobjects;

import com.huellapositiva.domain.exception.EmptyTemplateException;

public class EmailTemplate {
    private final String originalTemplate;
    private String parsedTemplate;

    private EmailTemplate(String template) {
        this.originalTemplate = template;
    }

    private EmailTemplate(String template, String parsedTemplate) {
        this.originalTemplate = template;
        this.parsedTemplate = parsedTemplate;
    }

    public static EmailTemplate createEmailTemplate(String template) {
        if (template.isEmpty()) {
            throw new EmptyTemplateException("Empty template not allowed");
        }
        return new EmailTemplate(template);
    }

    public EmailTemplate parse(EmailConfirmation emailConfirmation) {
        parsedTemplate = originalTemplate.replace("${URL}", emailConfirmation.getToken());
        return new EmailTemplate(originalTemplate, parsedTemplate);
    }

    public String getParsedTemplate() {
        return parsedTemplate;
    }
}
