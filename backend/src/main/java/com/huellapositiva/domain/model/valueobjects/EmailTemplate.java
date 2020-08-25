package com.huellapositiva.domain.model.valueobjects;

import com.huellapositiva.domain.exception.EmailNotValidException;

import java.util.Map;

public class EmailTemplate {
    private final String originalTemplate;
    private String parsedTemplate;

    public EmailTemplate(String template) {
       this(template, "");
    }

    private EmailTemplate(String template, String parsedTemplate) {
        this.originalTemplate = template;
        this.parsedTemplate = parsedTemplate;
    }

    public EmailTemplate parse(Map<String, String> variables) {
        this.parsedTemplate = originalTemplate;
        for (Map.Entry<String,String> entry : variables.entrySet()) {
            parsedTemplate = parsedTemplate.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        if (parsedTemplate.contains("${")){
            throw new EmailNotValidException("Failed to parse template " + originalTemplate + " with keys " + variables.keySet());
        }
        return new EmailTemplate(originalTemplate, parsedTemplate);
    }

    public String getParsedTemplate() {
        return parsedTemplate;
    }


}


