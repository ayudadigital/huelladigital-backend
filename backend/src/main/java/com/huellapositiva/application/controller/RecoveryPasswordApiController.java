package com.huellapositiva.application.controller;

import com.huellapositiva.application.exception.UserNotFound;
import com.huellapositiva.domain.actions.FetchCredentialsAction;
import com.huellapositiva.domain.exception.TimeForRecoveringPasswordExpiredException;
import net.bytebuddy.implementation.bytecode.Throw;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityNotFoundException;
import java.text.ParseException;
import java.util.UUID;

@Controller
@RequestMapping("/api/v1/restore-password")
public class RecoveryPasswordApiController {

    @Autowired
    FetchCredentialsAction credentialsAction;

    @GetMapping("/sendRecoveryPasswordEmail/{email}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendEmailRecovery(@PathVariable String email){
        try {
            credentialsAction.execute(email);
        } catch (UserNotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Email not found");
        }
    }

    @PostMapping("/changePassword")
    public void changePassword(@RequestParam("hash") String hash, @RequestParam("newPassword") String password){
        try {
            credentialsAction.executePasswordChanging(hash, password);
        } catch (TimeForRecoveringPasswordExpiredException e) {
            throw new ResponseStatusException(HttpStatus.LOCKED, "The resource is locked because time for recovery password has expired");
        }
    }

}
