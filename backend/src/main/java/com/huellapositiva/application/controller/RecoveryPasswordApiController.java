package com.huellapositiva.application.controller;

import com.huellapositiva.domain.actions.FetchCredentialsAction;
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
    public void sendEmailRecovery(@PathVariable String email){
        credentialsAction.execute(email);
    }


    @GetMapping("/{hash}")
    public void changePassword(@PathVariable UUID hash){
        // check time of email recovery password sent --> if not expired:
        // user inserts new password (get in the signature?)
        // action --> repository (set the new password in DB);
        // send email, password changed successfully
    }


}
