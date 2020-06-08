package com.huellapositiva.application.controller;

import com.huellapositiva.domain.actions.EmailConfirmationAction;
import com.huellapositiva.domain.actions.ResendEmailConfirmationAction;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/email-confirmation")
public class EmailConfirmationApiController {

    @Autowired
    private EmailConfirmationAction emailConfirmationAction;

    @Autowired
    private ResendEmailConfirmationAction resendEmailConfirmationAction;

    @GetMapping("/{hash}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmEmail(@PathVariable UUID hash) {
        emailConfirmationAction.execute(hash);
    }

    @GetMapping("/resend")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resendConfirmEmail(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {

        resendEmailConfirmationAction.execute(token);
    }
}
