package com.huellapositiva.application.controller;

import com.huellapositiva.application.exception.UserNotFoundException;
import com.huellapositiva.domain.actions.FetchCredentialsAction;
import com.huellapositiva.domain.exception.TimeForRecoveringPasswordExpiredException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


@Controller
@RequestMapping("/api/v1/restore-password")
public class RecoveryPasswordApiController {

    @Autowired
    FetchCredentialsAction credentialsAction;

    @Operation(
            summary = "Send an email to recovery password",
            description = "Fetch the credential, create an email with a link to recover password, send the email.",
            tags = {"contactPerson, volunteers, recoveryPassword"}
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "No content, email sent successfully."
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Not found, email not found in database."
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not fetch the user data due to a connectivity issue."
                    )
            }
    )
    @PostMapping("/sendRecoveryPasswordEmail")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendEmailRecovery(@RequestParam("email") String email){
        try {
            credentialsAction.executeGenerationRecoveryPasswordEmail(email);
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Email not found");
        }
    }

    @Operation(
            summary = "Update the password",
            description = "Receive the new password, update it in the database and send a confirmation email .",
            tags = {"contactPerson, volunteers, recoveryPassword"}
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "No content, password updated and confirmation email sent successfully."
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden, can not access to the resource because the time has expired."
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not fetch the user data due to a connectivity issue."
                    )
            }
    )
    @PostMapping("/changePassword/{hash}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@PathVariable("hash") String hash, @RequestParam("newPassword") String password){
        try {
            credentialsAction.executePasswordChanging(hash, password);
        } catch (TimeForRecoveringPasswordExpiredException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "The resource is locked because time for recovery password has expired");
        }
    }

}
