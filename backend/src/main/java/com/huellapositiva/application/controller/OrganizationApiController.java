package com.huellapositiva.application.controller;

import com.huellapositiva.application.dto.OrganizationRequestDto;
import com.huellapositiva.application.exception.UserNotFound;
import com.huellapositiva.domain.actions.RegisterOrganizationAction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.security.RolesAllowed;

@RestController
@AllArgsConstructor
@Tag(name = "Organizations", description = "The organization API")
@RequestMapping("/api/v1/organizations")
public class OrganizationApiController {

    private final RegisterOrganizationAction registerOrganizationAction;

    @Operation(
            summary = "Register a new organization",
            description = "Register a new organization and link it to the logged employee",
            tags = "user"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Ok, organization registered successful"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not register the organization.",
                            content = @Content()
                    )
            }
    )
    @PostMapping
    @RolesAllowed("ORGANIZATION_MEMBER")
    @ResponseBody
    public void registerOrganization(@RequestBody OrganizationRequestDto dto) {
        try {
            String employeeEmail = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            registerOrganizationAction.execute(dto, employeeEmail);
        } catch (UserNotFound ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not register the user caused by a connectivity issue");
        }
    }
}
