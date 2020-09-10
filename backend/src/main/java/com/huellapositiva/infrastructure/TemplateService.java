package com.huellapositiva.infrastructure;

import com.huellapositiva.domain.exception.TemplateNotAvailableException;
import com.huellapositiva.domain.model.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.model.valueobjects.EmailTemplate;
import com.huellapositiva.domain.model.valueobjects.ProposalRevisionEmail;
import com.huellapositiva.domain.model.valueobjects.ProposalRevisionRequestEmail;
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
        variables.put("PROPOSAL_URL", url );
        return new EmailTemplate(template).parse(variables);
    }

    public EmailTemplate getProposalRevisionTemplate(ProposalRevisionEmail proposalRevisionRequestEmail) {
        String relativePath = "classpath:templates/emails/proposalRevisionResponseRequest.txt";
        String template = getFileContent(relativePath);
        Map<String, String> variables = new HashMap<>();
        String url = proposalRevisionRequestEmail.getProposalURL();
        variables.put("PROPOSAL_URL", url );
        return new EmailTemplate(template).parse(variables);
    }


}