package com.huellapositiva.application.controller;

import com.huellapositiva.application.exception.UserNotFound;
import com.huellapositiva.domain.actions.FetchCredentialsAction;
import com.huellapositiva.domain.exception.TimeForRecoveringPasswordExpiredException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
    @GetMapping("/sendRecoveryPasswordEmail/{email}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendEmailRecovery(@PathVariable String email){
        try {
            credentialsAction.executeGenerationRecoveryPasswordEmail(email);
        } catch (UserNotFound e) {
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
                            responseCode = "423",
                            description = "Locked, can not access to the resource because the time has expired."
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not fetch the user data due to a connectivity issue."
                    )
            }
    )
    @PostMapping("/changePassword")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@RequestParam("hash") String hash, @RequestParam("newPassword") String password){
        try {
            credentialsAction.executePasswordChanging(hash, password);
        } catch (TimeForRecoveringPasswordExpiredException e) {
            throw new ResponseStatusException(HttpStatus.LOCKED, "The resource is locked because time for recovery password has expired");
        }
    }

}
