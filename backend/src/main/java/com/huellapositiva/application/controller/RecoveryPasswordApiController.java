package com.huellapositiva.application.controller;

import com.huellapositiva.application.exception.UserNotFound;
import com.huellapositiva.domain.actions.FetchCredentialsAction;
import com.huellapositiva.domain.exception.TimeForRecoveringPasswordExpiredException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityNotFoundException;
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
    public void changePassword(@RequestParam String hash, @RequestParam String password){
        /*
        1º. Ir a la base de datos y comprobar si ha expirado o no el tiempo del hash.
        2º. Buscar en la base de datos por hash y cambiar la password.
        Y si ha expirado
        3º. Le mandamos una excepción "ha expirado"
         */



        // check time of email recovery password sent --> if not expired:
        // user inserts new password (get in the signature?)
        // action --> repository (set the new password in DB);
        // send email, password changed successfully

        try {

        } catch (TimeForRecoveringPasswordExpiredException e) {

            // Le redireccionamos al endpoint para volver a empezar de nuevo para enviar el email, esto en front
            // De alguna forma hay que hacerle saber al front que el tiempo ha expirado.
        }
    }

}
