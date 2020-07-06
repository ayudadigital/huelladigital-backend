package com.huellapositiva.application.controller;

import com.huellapositiva.domain.actions.EmailConfirmationAction;
import com.huellapositiva.domain.actions.ResendEmailConfirmationAction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import java.util.UUID;

@RestController
@AllArgsConstructor
@Tag(name = "Email confirmation ", description = "The email confirmation API")
@RequestMapping("/api/v1/email-confirmation")
public class EmailConfirmationApiController {

    @Autowired
    private final EmailConfirmationAction emailConfirmationAction;

    @Autowired
    private final ResendEmailConfirmationAction resendEmailConfirmationAction;

    @Operation(
            summary = "Confirm email",
            description = "Verify the email of user",
            tags = "email"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Ok, verify email"
                    )
            }
    )
    @GetMapping("/{hash}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmEmail(@Parameter(description = "Hash linked with email confirmation") @PathVariable UUID hash) {
        emailConfirmationAction.execute(hash);
    }

    @Operation(
            summary = "Resend another has to confirmation email",
            description = "If the user without confirm the email need resend a new hash to confirm email",
            tags = "email"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Ok, resend a new hash"
                    )
            }
    )
    @RolesAllowed({"VOLUNTEER_NOT_CONFIRMED"})
    @PostMapping("/resend-email-confirmation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resendConfirmEmail() {
        resendEmailConfirmationAction.execute();
    }
}
