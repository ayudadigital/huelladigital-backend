package com.huellapositiva.application.controller;

import com.huellapositiva.application.exception.NoVolunteerSubscribedException;
import com.huellapositiva.domain.actions.ManageNewsletterExcelAction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;

@RestController
@AllArgsConstructor
@Tag(name = "Newsletter ", description = "The newsletter manager API")
@RequestMapping("/api/v1/newsletter")
public class NewsletterController {

    @Autowired
    private final ManageNewsletterExcelAction manageNewsletterExcelAction;

    @Operation(
            summary = "Download newsletter excel",
            description = "Prepare an excel with the volunteers subscribed and send it to reviser",
            tags = "newsletter",
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
                            description = "No content, email has been verified."
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Not found, not any volunteers subscribed to newsletter."
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not fetch the user data due to a connectivity issue."
                    )
            }
    )
    @GetMapping("/getNewsletterExcel")
    @RolesAllowed("REVISER")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendExcelLinkEmail(@Parameter(hidden = true) @AuthenticationPrincipal String reviserEmail) throws IOException {
        try {
            manageNewsletterExcelAction.execute(reviserEmail);
        } catch (IllegalStateException e) {
            throw new NoVolunteerSubscribedException(e.getMessage());
        }
    }
}
