package com.huellapositiva.domain.actions;

import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.infrastructure.orm.entities.EmailConfirmation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FetchCredentialsAction {

    @Autowired
    EmailCommunicationService emailCommunicationService;

    public void execute(EmailConfirmation emailAddress) {
        emailCommunicationService.sendRecoveryPasswordEmail(emailAddress);
    }
}
