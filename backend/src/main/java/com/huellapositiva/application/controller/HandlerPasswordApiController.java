package com.huellapositiva.application.controller;

import com.huellapositiva.application.dto.ChangePasswordDto;
import com.huellapositiva.application.exception.UserNotFoundException;
import com.huellapositiva.domain.actions.UpdatePasswordAction;
import com.huellapositiva.domain.exception.InvalidNewPasswordException;
import com.huellapositiva.domain.exception.NonMatchingPasswordException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;


@Controller
@RequestMapping("/api/v1/handling-password")
public class HandlerPasswordApiController {

    @Autowired
    UpdatePasswordAction credentialsAction;

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
            description = "The user access his profile to update his password. Roles allowed VOLUNTEER and CONTACT_PERSON.",
            tags = {"contactPerson, volunteers, profile"},
            parameters = {
                    @Parameter(name = "X-XSRF-TOKEN", in = ParameterIn.HEADER, required = true, example = "a6f5086d-af6b-464f-988b-7a604e46062b", description = "For take this value, open your inspector code on your browser, and take the value of the cookie with the name 'XSRF-TOKEN'. Example: a6f5086d-af6b-464f-988b-7a604e46062b"),
                    @Parameter(name = "XSRF-TOKEN", in = ParameterIn.COOKIE, required = true, example = "a6f5086d-af6b-464f-988b-7a604e46062b", description = "Same value of X-XSRF-TOKEN")
            },
            security = {
                    @SecurityRequirement(name = "accessToken")
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "No content, password updated and confirmation email sent successfully."
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad Request, the passwords do not match the regular expression, " +
                                    "or the length is out of 6-15 alphanumeric characters or is null."
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
    public void editProfilePassword(@Valid @RequestBody ChangePasswordDto dto,
                                    @Parameter(hidden = true) @AuthenticationPrincipal String accountId) {
        try {
            credentialsAction.executeUpdatePassword(dto, accountId);
        } catch (NonMatchingPasswordException | InvalidNewPasswordException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Password not valid: " + e.getMessage());
        }
    }
}
