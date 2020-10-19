package com.huellapositiva.domain.actions;

import com.huellapositiva.application.exception.UserNotFound;
import com.huellapositiva.domain.model.valueobjects.EmailRecoveryPassword;
import com.huellapositiva.domain.model.valueobjects.Token;
import com.huellapositiva.domain.service.EmailCommunicationService;

import com.huellapositiva.infrastructure.orm.entities.JpaCredential;
import com.huellapositiva.infrastructure.orm.repository.JpaCredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class FetchCredentialsAction {

    @Autowired
    EmailCommunicationService emailCommunicationService;

    @Autowired
    private JpaCredentialRepository jpaCredentialRepository;

    public void execute(String email) {
        JpaCredential jpaCredential = jpaCredentialRepository.findByEmail(email).orElseThrow(UserNotFound::new);
        String hash = Token.createToken().toString();
        jpaCredentialRepository.updateHashByEmail(email, hash);
        EmailRecoveryPassword emailRecoveryPassword = EmailRecoveryPassword.from(jpaCredential.getEmail(), hash);
        emailCommunicationService.sendRecoveryPasswordEmail(emailRecoveryPassword);
    }
}
