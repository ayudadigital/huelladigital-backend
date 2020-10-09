package com.huellapositiva.domain.service;

import com.huellapositiva.domain.model.valueobjects.*;
import com.huellapositiva.infrastructure.EmailService;
import com.huellapositiva.infrastructure.TemplateService;
import com.huellapositiva.infrastructure.orm.entities.JpaCredential;
import com.huellapositiva.infrastructure.orm.service.IssueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailCommunicationService {

    @Autowired
    private final EmailService emailService;

    @Autowired
    private final TemplateService templateService;

    @Autowired
    private final IssueService issueService;

    @Value("${huellapositiva.feature.email.from}")
    private String from;

    /**
     * This method parses an emailConfirmation and sends it to the volunteer/contactPerson
     *
     * @param emailConfirmation contains a hash and the volunteer/contactPerson email
     */
    public void sendRegistrationConfirmationEmail(EmailConfirmation emailConfirmation) {
        try {
            EmailTemplate emailTemplate = templateService.getEmailConfirmationTemplate(emailConfirmation);
            Email email = Email.createFrom(emailConfirmation, emailTemplate, from);
            emailService.sendEmail(email);
        } catch (RuntimeException ex) {
            log.error("Failed to send email:", ex);
            issueService.registerEmailConfirmationIssue(emailConfirmation.getEmailAddress(), ex);
        }
    }

    /**
     * This method parses a proposalRevisionRequestEmail and sends it to the reviser
     *
     * @param proposalRevisionRequestEmail contains the url to the proposal and the reviser email
     */
    public void sendRevisionRequestEmail(ProposalRevisionRequestEmail proposalRevisionRequestEmail) {
        EmailTemplate emailTemplate = templateService.getProposalRevisionRequestTemplate(proposalRevisionRequestEmail);
        Email email = Email.createFrom(proposalRevisionRequestEmail, emailTemplate, from);
        emailService.sendEmail(email);
    }

    /**
     * This method parses a proposalRevisionEmail with the revision from the reviser and sends it to the contactPerson
     *
     * @param proposalRevisionEmail contains the feedback and information about the revision
     */
    public void sendSubmittedProposalRevision(ProposalRevisionEmail proposalRevisionEmail) {
        EmailTemplate emailTemplate;
        if(proposalRevisionEmail.hasFeedback()) {
            emailTemplate = templateService.getProposalRevisionWithFeedbackTemplate(proposalRevisionEmail);
        } else {
            emailTemplate = templateService.getProposalRevisionWithoutFeedbackTemplate(proposalRevisionEmail);
        }
        Email email = Email.createFrom(proposalRevisionEmail, emailTemplate, from);
        emailService.sendEmail(email);
    }



    public void sendRecoveryPasswordEmail(JpaCredential jpaCredential) {
        try {
            EmailTemplate emailTemplate = templateService.getRecoveryEmailTemplate(jpaCredential.getHashedPassword());
            Email email = Email.createFrom(jpaCredential, emailTemplate, from);
            emailService.sendEmail(email);
        } catch (RuntimeException ex) {
            log.error("Failed to send email:", ex);
        }
    }
}
