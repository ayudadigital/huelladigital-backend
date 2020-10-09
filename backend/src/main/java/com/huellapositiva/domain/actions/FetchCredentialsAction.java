package com.huellapositiva.domain.actions;

import com.huellapositiva.application.exception.UserNotFound;
import com.huellapositiva.domain.model.valueobjects.EmailAddress;
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

    public void execute(EmailAddress emailAddress) {
        JpaCredential jpaCredential = jpaCredentialRepository.findByEmail(emailAddress.toString()).orElseThrow(UserNotFound::new);
        emailCommunicationService.sendRecoveryPasswordEmail(jpaCredential);
    }
}
