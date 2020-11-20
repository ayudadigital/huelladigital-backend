package com.huellapositiva.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.ChangePasswordDto;
import com.huellapositiva.application.exception.UserNotFoundException;
import com.huellapositiva.domain.actions.UpdatePasswordAction;
import com.huellapositiva.domain.exception.InvalidNewPasswordException;
import com.huellapositiva.domain.exception.NonMatchingPasswordException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;


@Controller
@RequestMapping("/api/v1/handling-password")
public class HandlerPasswordApiController {

    @Autowired
    UpdatePasswordAction credentialsAction;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
        credentialsAction.executePasswordChanging(hash, password);
    }

    @Operation(
            summary = "Update the password from profile",
            description = "The user access his profile to update his password.",
            tags = {"contactPerson, volunteers, profile"}
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "No content, password updated and confirmation email sent successfully."
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Conflict, the old password does not match or the new password is invalid."
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not fetch the user data due to a connectivity issue."
                    )
            }
    )
    @RolesAllowed({"VOLUNTEER", "CONTACT_PERSON"})
    @PostMapping("/editPassword")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void editProfilePassword(@RequestPart("dto") MultipartFile dtoMultipart,
                                    @AuthenticationPrincipal String email) throws IOException {
        ChangePasswordDto dto = objectMapper.readValue(dtoMultipart.getBytes(), ChangePasswordDto.class);
        try {
            credentialsAction.executeUpdatePassword(dto, email);
        } catch (NonMatchingPasswordException | InvalidNewPasswordException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Password not valid: " + e.getMessage());
        }
    }
}
