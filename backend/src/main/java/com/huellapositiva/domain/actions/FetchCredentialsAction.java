package com.huellapositiva.domain.actions;

import com.amazonaws.services.kms.model.ExpiredImportTokenException;
import com.huellapositiva.application.exception.UserNotFound;
import com.huellapositiva.domain.exception.TimeForRecoveringPasswordExpiredException;
import com.huellapositiva.domain.model.valueobjects.*;
import com.huellapositiva.domain.service.EmailCommunicationService;

import com.huellapositiva.infrastructure.orm.entities.JpaCredential;
import com.huellapositiva.infrastructure.orm.repository.JpaCredentialRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;


@Service
@AllArgsConstructor
public class FetchCredentialsAction {

    @Autowired
    EmailCommunicationService emailCommunicationService;

    @Autowired
    private JpaCredentialRepository jpaCredentialRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    public void execute(String email) {
        JpaCredential jpaCredential = jpaCredentialRepository.findByEmail(email).orElseThrow(UserNotFound::new);
        String hash = Token.createToken().toString();
        jpaCredentialRepository.updateHashByEmail(email, hash);
        EmailRecoveryPassword emailRecoveryPassword = EmailRecoveryPassword.from(jpaCredential.getEmail(), hash);
        emailCommunicationService.sendRecoveryPasswordEmail(emailRecoveryPassword);
    }

    public void executePasswordChanging(String hash, String password) {
        JpaCredential jpaCredential = jpaCredentialRepository.findByHashRecoveryPassword(hash).orElseThrow(UserNotFound::new);
        Date timeOfExpiration = addAnHour(jpaCredential.getCreatedRecoveryHashOn());
        Date dateNow = Calendar.getInstance().getTime();

        if (timeOfExpiration.after(dateNow)) {
            PasswordHash passwordHash = new PasswordHash(passwordEncoder.encode(password));
            jpaCredentialRepository.updatePassword(passwordHash.toString(), jpaCredential.getEmail());
            jpaCredentialRepository.updateRecoveryPasswordHashAndDate(jpaCredential.getEmail(), null, null);

            EmailAddress emailAddress = EmailAddress.from(jpaCredential.getEmail());
            emailCommunicationService.sendConfirmationPasswordChanged(emailAddress);
        } else {
            throw new TimeForRecoveringPasswordExpiredException("The time to recovery password has expired");
        }
    }

    private Date addAnHour(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.HOUR, 1);  // number of days to add
        return c.getTime();
    }


}
