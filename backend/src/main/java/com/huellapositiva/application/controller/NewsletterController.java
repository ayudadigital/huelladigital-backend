package com.huellapositiva.application.controller;

import com.huellapositiva.domain.actions.ManageNewsletterExcelAction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
            description = "Prepare an excel with the volunteers subscribed and return it",
            tags = "newsletter"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Ok, email has been verified"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not fetch the user data due to a connectivity issue."
                    )
            }
    )
    @GetMapping("/download")
    @RolesAllowed("REVISER")
    @ResponseStatus(HttpStatus.OK)
    public void downloadExcel() throws IOException {
        manageNewsletterExcelAction.execute();
    }
}
