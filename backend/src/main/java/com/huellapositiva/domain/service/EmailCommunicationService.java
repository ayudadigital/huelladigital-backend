package com.huellapositiva.domain.service;

import com.huellapositiva.domain.model.valueobjects.*;
import com.huellapositiva.infrastructure.EmailService;
import com.huellapositiva.infrastructure.TemplateService;
import com.huellapositiva.infrastructure.orm.service.IssueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;

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
     * In case email is not sent, an error message is stored in the database to detect possible
     * issues during the volunteer/contactPerson registration
     *
     * @param emailConfirmation contains a hash and the volunteer/contactPerson email
     */
    public void sendRegistrationConfirmationEmail(EmailConfirmation emailConfirmation) {
        final String CONFIRMATION_EMAIL_SUBJECT = "Confirmación de la cuenta en huellapositiva";

        try {
            EmailTemplate emailTemplate = templateService.getEmailConfirmationTemplate(emailConfirmation);
            EmailMessage emailMessage = EmailMessage.createFrom(from, emailConfirmation.getEmailAddress(),
                                                                CONFIRMATION_EMAIL_SUBJECT, emailTemplate);
            emailService.sendEmail(emailMessage);
        } catch (RuntimeException ex) {
            log.error("Failed to send emailregisterVolunteerIssue:", ex);
            issueService.registerEmailConfirmationIssue(emailConfirmation.getEmailAddress(), ex);
        }
    }

    /**
     * This method parses a proposalRevisionRequestEmail and sends it to the reviser
     *
     * @param proposalRevisionRequestEmail contains the url to the proposal and the reviser email
     */
    public void sendRevisionRequestEmail(ProposalRevisionRequestEmail proposalRevisionRequestEmail) {
        final String REVISION_REQUIRED_SUBJECT = "Revisión de nuevas convocatorias requerida";
        EmailTemplate emailTemplate = templateService.getProposalRevisionRequestTemplate(proposalRevisionRequestEmail);
        EmailMessage emailMessage = EmailMessage.createFrom(from, proposalRevisionRequestEmail.getEmailAddress(), REVISION_REQUIRED_SUBJECT, emailTemplate);
        emailService.sendEmail(emailMessage);
    }

    /**
     * This method parses a proposalRevisionEmail with the revision from the reviser and sends it to the contactPerson
     *
     * @param proposalRevisionEmail contains the feedback and information about the revision
     */
    public void sendSubmittedProposalRevision(ProposalRevisionEmail proposalRevisionEmail) {
        final String FEEDBACK_REVISION_SUBJECT = "Revisión de tu convocatoria";
        EmailTemplate emailTemplate;
        if(proposalRevisionEmail.hasFeedback()) {
            emailTemplate = templateService.getProposalRevisionWithFeedbackTemplate(proposalRevisionEmail);
        } else {
            emailTemplate = templateService.getProposalRevisionWithoutFeedbackTemplate(proposalRevisionEmail);
        }
        EmailMessage emailMessage = EmailMessage.createFrom(from, proposalRevisionEmail.getEmailAddress(), FEEDBACK_REVISION_SUBJECT, emailTemplate);
        emailService.sendEmail(emailMessage);
    }

    public void sendRecoveryPasswordEmail(RecoveryPasswordEmail recoveryPasswordEmail) {
        final String RECOVERY_PASS_SUBJECT = "Cambio de tu contraseña";
        EmailTemplate emailTemplate = templateService.getRecoveryEmailTemplate(recoveryPasswordEmail.getHash());
        EmailMessage emailMessage = EmailMessage.createFrom(from, recoveryPasswordEmail.getEmail(), RECOVERY_PASS_SUBJECT, emailTemplate);
        emailService.sendEmail(emailMessage);
    }

    public void sendConfirmationPasswordChanged(EmailAddress emailAddress) {
        final String UPDATE_PASS_SUBJECT = "Confirmacion de cambio de contraseña";
        EmailTemplate emailTemplate = templateService.getConfirmationPasswordChangedTemplate();
        EmailMessage emailMessage = EmailMessage.createFrom(from, emailAddress.toString(), UPDATE_PASS_SUBJECT, emailTemplate);
        emailService.sendEmail(emailMessage);
    }

    public void sendMessageEmailChanged(EmailConfirmation emailConfirmation) {
        final String UPDATE_EMAIL_SUBJECT = "Información sobre cambio de email";
        EmailTemplate emailTemplate = templateService.getEmailChangedTemplate(emailConfirmation);
        EmailMessage emailMessage = EmailMessage.createFrom(from, emailConfirmation.getEmailAddress(),
                UPDATE_EMAIL_SUBJECT, emailTemplate);
        emailService.sendEmail(emailMessage);
    }

    public void sendMessageUpdateProposal(ProposalRevisionRequestEmail proposalRevisionRequestEmail) {
        final String UPDATE_EMAIL_SUBJECT = "Modificaciones en una propuesta de voluntariado";
        EmailTemplate emailTemplate = templateService.getEmailUpdateProposalTemplate(proposalRevisionRequestEmail);
        EmailMessage emailMessage = EmailMessage.createFrom(from, proposalRevisionRequestEmail.getEmailAddress(),
                UPDATE_EMAIL_SUBJECT, emailTemplate);
        emailService.sendEmail(emailMessage);
    }

    public void sendNewsletterSubscriptorsEmail(EmailAddress emailAddress, URL url) {
        final String NEWSLETTER_EMAIL_SUBJECT = "Descarga de excel con voluntarios suscritos a la Newsletter";
        EmailTemplate emailTemplate = templateService.getNewsletterEmailTemplate(url);
        EmailMessage emailMessage = EmailMessage.createFrom(from, emailAddress.toString(),
                NEWSLETTER_EMAIL_SUBJECT, emailTemplate);
        emailService.sendEmail(emailMessage);
    }

    public void sendMessageProposalPublished(String email, String proposalTitle) {
        final String PROPOSAL_PUBLISHED_EMAIL_SUBJECT = "Convocatoria publicada";
        EmailTemplate emailTemplate = templateService.getProposalPublishedTemplate(proposalTitle);
        EmailMessage emailMessage = EmailMessage.createFrom(from, email,
                PROPOSAL_PUBLISHED_EMAIL_SUBJECT, emailTemplate);
        emailService.sendEmail(emailMessage);
    }

    public void sendInadequateProposalEmail(EmailAddress emailAddress, String title, String reason) {
        final String INADEQUATE_PROPOSAL_EMAIL_SUBJECT = "Descarga de excel con voluntarios suscritos a la Newsletter";
        EmailTemplate emailTemplate = templateService.getInadequateProposalEmailTemplate(title, reason);
        EmailMessage emailMessage = EmailMessage.createFrom(from, emailAddress.toString(),
                INADEQUATE_PROPOSAL_EMAIL_SUBJECT, emailTemplate);
        emailService.sendEmail(emailMessage);
    }
}