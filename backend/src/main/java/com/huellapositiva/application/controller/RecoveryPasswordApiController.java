package com.huellapositiva.application.controller;

import com.huellapositiva.domain.actions.FetchCredentialsAction;
import com.huellapositiva.domain.model.valueobjects.EmailAddress;
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
    public void sendEmailRecovery(EmailAddress emailAddress){
        credentialsAction.execute(emailAddress);
        // A partir de la cuenta de email --> recuperar el hash
        //  añadir ese hash al enlace en el email de recuperación
        // send email through the service
    }

    @GetMapping("/{hash}")
    public void changePassword(@PathVariable UUID hash){
        // user inserts new password (get in the signature?)
        // action --> repository (set the new password in DB);
        // send email, password changed successfully
    }


}
