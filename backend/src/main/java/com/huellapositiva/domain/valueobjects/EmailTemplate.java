package com.huellapositiva.domain.valueobjects;

public class EmailTemplate {
    private final String originalTemplate;
    private String parsedTemplate;

    public EmailTemplate(String template) {
        this.originalTemplate = template;
    }

    private EmailTemplate(String template, String parsedTemplate) {
        this.originalTemplate = template;
        this.parsedTemplate = parsedTemplate;
    }

    public EmailTemplate parse(EmailConfirmation emailConfirmation) {
        parsedTemplate = originalTemplate.replace("${URL}", emailConfirmation.getToken());
        return new EmailTemplate(originalTemplate, parsedTemplate);
    }

    public static EmailTemplate parseAuxiliarTemplate(EmailConfirmation emailConfirmation){
        String originalTemplate = "Te acabas de registrar en huellapositiva.com\n" +
                "Para confirmar tu correo electrónico, haz clic en el enlace\n" +
                "<a href=\"${URL}\">Clic aquí</a>";
        EmailTemplate emailTemplate = new EmailTemplate(originalTemplate);
        return emailTemplate.parse(emailConfirmation);
    }

    public String getParsedTemplate() {
        return parsedTemplate;
    }
}


