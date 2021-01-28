package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.ChangePasswordDto;
import com.huellapositiva.application.exception.UserNotFoundException;
import com.huellapositiva.domain.exception.InvalidNewPasswordException;
import com.huellapositiva.domain.exception.NonMatchingPasswordException;
import com.huellapositiva.domain.exception.TimeForRecoveringPasswordExpiredException;
import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.valueobjects.PasswordHash;
import com.huellapositiva.domain.model.valueobjects.RecoveryPasswordEmail;
import com.huellapositiva.domain.model.valueobjects.Token;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.infrastructure.orm.entities.JpaCredential;
import com.huellapositiva.infrastructure.orm.repository.JpaCredentialRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class UpdatePasswordAction {

    @Autowired
    private final EmailCommunicationService emailCommunicationService;

    @Autowired
    private final JpaCredentialRepository jpaCredentialRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    /**
     * This method generates a link to recovery the password and sends it
     *
     * @param email Email account from de volunteer or contact person.
     */
    public void executeGenerationRecoveryPasswordEmail(String email) {
        JpaCredential jpaCredential = jpaCredentialRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        String hash = Token.createToken().toString();
        jpaCredentialRepository.updateHashByEmail(email, hash);
        RecoveryPasswordEmail recoveryPasswordEmail = RecoveryPasswordEmail.from(jpaCredential.getEmail(), hash);
        emailCommunicationService.sendRecoveryPasswordEmail(recoveryPasswordEmail);
    }

    /**
     * This method updates the password, updates the hashRecoveryPassword and createdRecoveryHashOn to null in database
     * and sends a confirmation email.
     *
     * @param hashRecoveryPassword Unique identifier to recover password
     * @param password New password
     */
    public void executePasswordChanging(String hashRecoveryPassword, String password) {
        JpaCredential jpaCredential = jpaCredentialRepository.findByHashRecoveryPassword(hashRecoveryPassword).orElseThrow(UserNotFoundException::new);
        LocalDateTime timeOfExpiration = jpaCredential.getCreatedRecoveryHashOn().plusHours(1);
        LocalDateTime dateNow = LocalDateTime.now();

        if (timeOfExpiration.isAfter(dateNow)) {
            PasswordHash passwordHash = new PasswordHash(passwordEncoder.encode(password));
            jpaCredentialRepository.updatePassword(passwordHash.toString(), jpaCredential.getEmail());
            jpaCredentialRepository.setRecoveryPasswordHashAndDate(jpaCredential.getEmail(), null, null);

            EmailAddress emailAddress = EmailAddress.from(jpaCredential.getEmail());
            emailCommunicationService.sendConfirmationPasswordChanged(emailAddress);
        } else {
            throw new TimeForRecoveringPasswordExpiredException("The time to recovery password has expired");
        }
    }

    /**
     * This method updates the password in database from the profile and sends an email.
     *
     * @param dto an object with de old password and the new password
     * @param accountId The account ID of the user
     */
    public void executeUpdatePassword(ChangePasswordDto dto, String accountId) {

        JpaCredential jpaCredential = jpaCredentialRepository.findByAccountId(accountId).orElseThrow(UserNotFoundException::new);
        PasswordHash newPasswordHash = new PasswordHash(passwordEncoder.encode(dto.getNewPassword()));

        if (!passwordEncoder.matches(dto.getOldPassword(), jpaCredential.getHashedPassword())) {
            throw new NonMatchingPasswordException("The old password inserted does not match with the one stored in the system");
        } else if (passwordEncoder.matches(dto.getNewPassword(), jpaCredential.getHashedPassword())) {
            throw new InvalidNewPasswordException("The new password it exactly the same as the old password");
        }

        jpaCredentialRepository.updatePassword(newPasswordHash.toString(), accountId);

        EmailAddress emailAddress = EmailAddress.from(jpaCredential.getEmail());
        emailCommunicationService.sendConfirmationPasswordChanged(emailAddress);
    }
}
