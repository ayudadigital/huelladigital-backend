package com.huellapositiva.application.controller;

import com.huellapositiva.domain.actions.EmailConfirmationAction;
import com.huellapositiva.domain.actions.ResendEmailConfirmationAction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
                            description = "Ok, email has been verified"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Not found, the hash does not exist"
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Conflict, the email you are trying to confirm is already confirmed"
                    ),
                    @ApiResponse(
                            responseCode = "410",
                            description = "Gone, the email has already expired, you need to generate a new email confirmation using */resend-email-confirmation* endpoint"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not fetch the user data due to a connectivity issue."
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
            description = "If the user without confirm the email need resend a new hash to confirm email. Roles allowed VOLUNTEER_NOT_CONFIRMED.",
            tags = "email",
            parameters = {
                    @Parameter(name = "X-XSRF-TOKEN", in = ParameterIn.HEADER, required = true, example = "a6f5086d-af6b-464f-988b-7a604e46062b", description = "For take this value, open your inspector code on your browser, and take the value of the cookie with the name 'XSRF-TOKEN'. Example: a6f5086d-af6b-464f-988b-7a604e46062b"),
                    @Parameter(name = "XSRF-TOKEN", in = ParameterIn.COOKIE, required = true, example = "a6f5086d-af6b-464f-988b-7a604e46062b", description = "Same value of X-XSRF-TOKEN"),
            },
            security = {
                    @SecurityRequirement(name = "accessToken")
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Ok, email has been resent successfully"
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized, you need a valid access token",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden, you need a valid XSRF-TOKEN"
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Conflict, the email you are trying to resend is already confirmed"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not register the ESAL."
                    )
            }
    )
    @RolesAllowed({"VOLUNTEER_NOT_CONFIRMED"})
    @PostMapping("/resend-email-confirmation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resendConfirmEmail(@Parameter(hidden = true) @AuthenticationPrincipal String accountId) {
        resendEmailConfirmationAction.execute(accountId);
    }
}
