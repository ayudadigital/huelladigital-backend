package com.huellapositiva.infrastructure;

import com.huellapositiva.application.exception.UserNotFoundException;
import com.huellapositiva.domain.exception.TemplateNotAvailableException;
import com.huellapositiva.domain.model.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.model.valueobjects.EmailTemplate;
import com.huellapositiva.domain.model.valueobjects.ProposalRevisionEmail;
import com.huellapositiva.domain.model.valueobjects.ProposalRevisionRequestEmail;
import com.huellapositiva.domain.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class TemplateService {

    public static final String PROPOSAL_URL ="PROPOSAL_URL";

    private String getFileContent(String relativePath) {
        try {
            return FileUtils.getResourceContent(relativePath);
        } catch (IOException ex) {
            throw new TemplateNotAvailableException(ex);
        }
    }

    public EmailTemplate getEmailConfirmationTemplate(EmailConfirmation emailConfirmation) {
        String relativePath = "templates/emails/emailConfirmation.txt";
        String template = getFileContent(relativePath);
        Map<String, String> variables = new HashMap<>();
        String url = emailConfirmation.getUrl();
        variables.put("CONFIRMATION_URL", url );
        return new EmailTemplate(template).parse(variables);
    }

    public EmailTemplate getProposalRevisionRequestTemplate(ProposalRevisionRequestEmail proposalRevisionRequestEmail) {
        String relativePath = "templates/emails/proposalRevisionRequest.txt";
        String template = getFileContent(relativePath);
        Map<String, String> variables = new HashMap<>();
        String url = proposalRevisionRequestEmail.getProposalUrl();
        variables.put(PROPOSAL_URL, url );
        return new EmailTemplate(template).parse(variables);
    }

    public EmailTemplate getProposalRevisionWithFeedbackTemplate(ProposalRevisionEmail proposalRevisionRequestEmail) {
        if (proposalRevisionRequestEmail.getReviser() == null) {
            throw new UserNotFoundException("Reviser was not found.");
        }

        String relativePath = "templates/emails/proposalRevisionResponseWithFeedbackRequest.txt";
        String template = getFileContent(relativePath);
        Map<String, String> variables = new HashMap<>();
        variables.put(PROPOSAL_URL, proposalRevisionRequestEmail.getProposalURL());
        String contactPersonName = proposalRevisionRequestEmail.getEsalContactPerson().getFullName();
        variables.put("CONTACT_PERSON_NAME", contactPersonName != null ? contactPersonName : "usuario de Huella Positiva");
        String reviserName = proposalRevisionRequestEmail.getReviser().getFullName();
        variables.put("REVISER_NAME",  reviserName != null ? reviserName : "un revisor");
        variables.put("REVISION_FEEDBACK", proposalRevisionRequestEmail.getFeedback());
        return new EmailTemplate(template).parse(variables);
    }

    public EmailTemplate getProposalRevisionWithoutFeedbackTemplate(ProposalRevisionEmail proposalRevisionRequestEmail) {
        String relativePath = "templates/emails/proposalRevisionResponseWithoutFeedbackRequest.txt";
        String template = getFileContent(relativePath);
        Map<String, String> variables = new HashMap<>();
        variables.put(PROPOSAL_URL, proposalRevisionRequestEmail.getProposalURL());
        String contactPersonName = proposalRevisionRequestEmail.getEsalContactPerson().getFullName();
        variables.put("CONTACT_PERSON_NAME", contactPersonName != null ? contactPersonName : "usuario de Huella Positiva");
        String reviserName = proposalRevisionRequestEmail.getReviser().getFullName();
        variables.put("REVISER_NAME",  reviserName != null ? reviserName : "un revisor");
        return new EmailTemplate(template).parse(variables);
    }

    public EmailTemplate getRecoveryEmailTemplate(String hashPassword){
        String relativePath = "templates/emails/recoveryPasswordEmail.txt";
        String template = getFileContent(relativePath);
        Map<String, String> variables = new HashMap<>();
        String url = "/api/v1/restore-password/" + hashPassword;
        variables.put("RECOVERY_PASSWORD_URL", url);
        return new EmailTemplate(template).parse(variables);
    }

    public EmailTemplate getConfirmationPasswordChangedTemplate(){
        String relativePath = "templates/emails/confirmationPasswordChanged.txt";
        String template = getFileContent(relativePath);
        return new EmailTemplate(template);
    }

    public EmailTemplate getEmailChangedTemplate(EmailConfirmation emailConfirmation){
        String relativePath = "templates/emails/emailChange.txt";
        String template = getFileContent(relativePath);
        Map<String, String> variables = new HashMap<>();
        String url = emailConfirmation.getUrl();
        variables.put("CONFIRMATION_URL", url );
        return new EmailTemplate(template).parse(variables);
    }

    public EmailTemplate getEmailUpdateProposalTemplate(ProposalRevisionRequestEmail proposalRevisionEmail){
        String relativePath = "templates/emails/updateProposalRequest.txt";
        String template = getFileContent(relativePath);
        Map<String, String> variables = new HashMap<>();
        String url = proposalRevisionEmail.getProposalUrl();
        variables.put("PROPOSAL_URL_INFORMATION", url);
        return new EmailTemplate(template).parse(variables);
    }

    public EmailTemplate getNewsletterEmailTemplate(URL url){
        String relativePath = "templates/emails/newsletterEmail.txt";
        String template = getFileContent(relativePath);
        Map<String, String> variables = new HashMap<>();
        String stringUrl = url.toString();
        variables.put("NEWSLETTER_URL", stringUrl);
        return new EmailTemplate(template).parse(variables);
    }

    public EmailTemplate getProposalPublishedTemplate(String proposalTitle) {
        String relativePath = "templates/emails/proposalPublished.txt";
        String template = getFileContent(relativePath);
        Map<String, String> variables = new HashMap<>();
        variables.put("PROPOSAL_TITLE", proposalTitle );
        return new EmailTemplate(template).parse(variables);
    }

    public EmailTemplate getInadequateProposalEmailTemplate(String title, String reason){
        String relativePath = "templates/emails/inadequateProposalEmail.txt";
        String template = getFileContent(relativePath);
        Map<String, String> variables = new HashMap<>();
        variables.put("TITLE", title);
        variables.put("REASON", reason);
        return new EmailTemplate(template).parse(variables);
    }
}