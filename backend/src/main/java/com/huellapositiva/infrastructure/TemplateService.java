package com.huellapositiva.infrastructure;

import com.huellapositiva.domain.exception.TemplateNotAvailableException;
import com.huellapositiva.domain.model.valueobjects.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class TemplateService {

    public static final String PROPOSAL_URL ="PROPOSAL_URL";

    private String getFileContent(String relativePath) {
        try {
            File file = ResourceUtils.getFile(relativePath);
            return String.join("\n", Files.readAllLines(file.toPath()));
        } catch (IOException ex) {
            log.error("Failed to open file {}", relativePath, ex);
            throw new TemplateNotAvailableException(ex);
        }
    }

    public EmailTemplate getEmailConfirmationTemplate(EmailConfirmation emailConfirmation) {
        String relativePath = "classpath:templates/emails/emailConfirmation.txt";
        String template = getFileContent(relativePath);
        Map<String, String> variables = new HashMap<>();
        String url = emailConfirmation.getUrl();
        variables.put("CONFIRMATION_URL", url );
        return new EmailTemplate(template).parse(variables);
    }

    public EmailTemplate getProposalRevisionRequestTemplate(ProposalRevisionRequestEmail proposalRevisionRequestEmail) {
        String relativePath = "classpath:templates/emails/proposalRevisionRequest.txt";
        String template = getFileContent(relativePath);
        Map<String, String> variables = new HashMap<>();
        String url = proposalRevisionRequestEmail.getProposalUrl();
        variables.put(PROPOSAL_URL, url );
        return new EmailTemplate(template).parse(variables);
    }

    public EmailTemplate getProposalRevisionWithFeedbackTemplate(ProposalRevisionEmail proposalRevisionRequestEmail) {
        String relativePath = "classpath:templates/emails/proposalRevisionResponseWithFeedbackRequest.txt";
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
        String relativePath = "classpath:templates/emails/proposalRevisionResponseWithoutFeedbackRequest.txt";
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
        String relativePath = "classpath:templates/emails/recoveryPasswordEmail.txt";
        String template = getFileContent(relativePath);
        Map<String, String> variables = new HashMap<>();
        String url = "/api/v1/restore-password/" + hashPassword;
        variables.put("RECOVERY_PASSWORD_URL", url);
        return new EmailTemplate(template).parse(variables);
    }

    public EmailTemplate getConfirmationPasswordChangedTemplate(){
        String relativePath = "classpath:templates/emails/confirmationPasswordChanged.txt";
        String template = getFileContent(relativePath);
        return new EmailTemplate(template);
    }
}