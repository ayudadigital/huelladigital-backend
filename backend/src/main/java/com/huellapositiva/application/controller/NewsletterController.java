package com.huellapositiva.application.controller;

import com.huellapositiva.domain.actions.ChangeStatusNewsletterSubscriptionAction;
import com.huellapositiva.domain.actions.ManageNewsletterExcelAction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;

@RestController
@AllArgsConstructor
@Tag(name = "Newsletter ", description = "The newsletter manager API")
@RequestMapping("/api/v1/newsletter")
public class NewsletterController {

    @Autowired
    private final ManageNewsletterExcelAction manageNewsletterExcelAction;

    @Autowired
    private final ChangeStatusNewsletterSubscriptionAction changeStatusNewsletterSubscriptionAction;

    @Operation(
            summary = "Change status of subscription to newsletter",
            description = "Changes the status of the subscribed parameter on the specified volunteer",
            tags = "newsletter"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Ok, status of subscribed field changed successfully"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not fetch the user data due to a connectivity issue."
                    )
            }
    )
    @PostMapping("/changeStatusNewsletterSubscription")
    @RolesAllowed("VOLUNTEER")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public void changeStatusNewsletterSubscription(@RequestBody Boolean subscribed,
                                                   @AuthenticationPrincipal String volunteerEmail) {
        changeStatusNewsletterSubscriptionAction.execute(subscribed, volunteerEmail);
    }

    @Operation(
            summary = "Download newsletter excel",
            description = "Prepare an excel with the volunteers subscribed and send it to reviser",
            tags = "newsletter"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "No content, email has been verified"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not fetch the user data due to a connectivity issue."
                    )
            }
    )
    @GetMapping("/download")
    @RolesAllowed("REVISER")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void downloadExcel(@AuthenticationPrincipal String reviserEmail) throws IOException {
        manageNewsletterExcelAction.execute(reviserEmail);
    }
}
