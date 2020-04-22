package com.huellapositiva.domain.valueobjects;

public class EmailTemplate {
    private final String originalTemplate;
    private String parsedTemplate;

    public EmailTemplate(String template) {
        this.originalTemplate = template;
    }

    private EmailTemplate(String template,String parsedTemplate) {
        this.originalTemplate = template;
        this.parsedTemplate = parsedTemplate;
    }

    public EmailTemplate parse(EmailConfirmation emailConfirmation) {
        parsedTemplate = originalTemplate.replace("${URL}",emailConfirmation.getToken());
        return new EmailTemplate(originalTemplate,parsedTemplate);
    }

    public String getParsedTemplate() {
        return parsedTemplate;
    }
}
