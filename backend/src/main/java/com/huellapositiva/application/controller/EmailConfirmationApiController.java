package com.huellapositiva.application.controller;

import com.huellapositiva.application.exception.EmailConfirmationExpired;
import com.huellapositiva.domain.actions.EmailConfirmationAction;
import com.huellapositiva.domain.actions.ResendEmailConfirmationAction;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.security.RolesAllowed;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/email-confirmation")
public class EmailConfirmationApiController {

    @Autowired
    private final EmailConfirmationAction emailConfirmationAction;

    @Autowired
    private final ResendEmailConfirmationAction resendEmailConfirmationAction;

    @GetMapping("/{hash}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmEmail(@PathVariable UUID hash) {
        try {
            emailConfirmationAction.execute(hash);
        }catch (EmailConfirmationExpired e){
            throw new ResponseStatusException(HttpStatus.GONE, "Email confirmation has expired");
        }
    }

    @RolesAllowed({"VOLUNTEER_NOT_CONFIRMED"})
    @PostMapping("/resend-email-confirmation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resendConfirmEmail() {
        resendEmailConfirmationAction.execute();
    }
}
