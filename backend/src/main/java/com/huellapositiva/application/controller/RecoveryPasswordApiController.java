package com.huellapositiva.application.controller;

import com.huellapositiva.domain.actions.FetchCredentialsAction;
import com.huellapositiva.infrastructure.orm.entities.EmailConfirmation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.UUID;

@Controller
@RequestMapping("/api/v1/restore-password")
public class RecoveryPasswordApiController {

    @Autowired
    FetchCredentialsAction credentialsAction;

    @GetMapping("/email")
    public void sendEmailRecovery(EmailConfirmation emailAddress){
        credentialsAction.execute(emailAddress);
        // send email through the service
        // In the email send an unique hash/UUID to identify the user.
        // this hash will be added to the endpoint
    }

    @GetMapping("/change-password/{hash}")
    public void changePassword(@PathVariable UUID hash){
        // user inserts new password (get in the signature?)
        // action --> repository (set the new password in DB);
        // send email, password changed successfully
    }


}
