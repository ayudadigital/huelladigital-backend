package com.huellapositiva.application.controller;

import com.huellapositiva.application.dto.ESALRequestDto;
import com.huellapositiva.application.exception.UserNotFound;
import com.huellapositiva.domain.actions.RegisterESALAction;
import com.huellapositiva.domain.exception.UserAlreadyHasESALException;
import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import com.huellapositiva.application.exception.ESALAlreadyExists;
import com.huellapositiva.domain.actions.DeleteESALAction;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.security.RolesAllowed;

@RestController
@AllArgsConstructor
@Tag(name = "ESAL", description = "The ESAL api.")
@RequestMapping("/api/v1/esal")
public class ESALApiController {

    private final RegisterESALAction registerESALAction;

    private final DeleteESALAction deleteESALAction;

    @Operation(
            summary = "Register a new ESAL",
            description = "Register a new ESAL and link it to the logged employee",
            tags = "ESAL",
            parameters = {
                    @Parameter(name = "X-XSRF-TOKEN", in = ParameterIn.HEADER, required = true, example = "a6f5086d-af6b-464f-988b-7a604e46062b", description = "For take this value, open your inspector code on your browser, and take the value of the cookie with the name 'XSRF-TOKEN'. Example: a6f5086d-af6b-464f-988b-7a604e46062b"),
                    @Parameter(name = "XSRF-TOKEN", in = ParameterIn.COOKIE,required = true, example = "a6f5086d-af6b-464f-988b-7a604e46062b", description = "Same value of X-XSRF-TOKEN")
            },
            security = {
                    @SecurityRequirement(name = "accessToken")
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Ok, ESAL registered successfully."
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Conflict, the provided name is already taken.",
                            content = @Content()
                    ),
                    @ApiResponse(
                            responseCode = "412",
                            description = "Precondition failed, the user attempting to create the ESAL has another one linked.",
                            content = @Content()
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not register the ESAL.",
                            content = @Content()
                    )
            }
    )
    @PostMapping
    @RolesAllowed("ORGANIZATION_MEMBER")
    @ResponseBody
    public void registerESAL(@RequestBody ESALRequestDto dto, @AuthenticationPrincipal String memberEmail) {
        try {
            registerESALAction.execute(dto, EmailAddress.from(memberEmail));
        } catch (ESALAlreadyExists ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "ESAL named " + dto.getName() + " already exists.");
        } catch (UserAlreadyHasESALException ex) {
            throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "The user attempting to create the ESAL has already registered another one.");
        } catch (UserNotFound ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not register the user caused by a connectivity issue");
        }
    }

    @Operation(
            summary = "Delete an ESAL",
            description = "Delete an ESAL and unlink their members, including their contact person.",
            tags = "ESAL",
            parameters = {
                    @Parameter(name = "X-XSRF-TOKEN", in = ParameterIn.HEADER, required = true, example = "a6f5086d-af6b-464f-988b-7a604e46062b", description = "For take this value, open your inspector code on your browser, and take the value of the cookie with the name 'XSRF-TOKEN'. Example: a6f5086d-af6b-464f-988b-7a604e46062b"),
                    @Parameter(name = "XSRF-TOKEN", in = ParameterIn.COOKIE,required = true, example = "a6f5086d-af6b-464f-988b-7a604e46062b", description = "Same value of X-XSRF-TOKEN")
            },
            security = {
                    @SecurityRequirement(name = "accessToken")
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Ok, ESAL deleted successfully."
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not register the ESAL.",
                            content = @Content()
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized, the user has no permissions to delete the ESAL.",
                            content = @Content()
                    )
            }
    )
    @DeleteMapping("/{id}")
    @RolesAllowed("ORGANIZATION_MEMBER")
    @ResponseBody
    public void deleteOrganization(@AuthenticationPrincipal String memberEmail, @PathVariable Integer id) {
        try {
            deleteESALAction.execute(memberEmail, id);
        } catch (UserNotFound ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not delete the ESAL caused by a connectivity issue.");
        }
    }

    @Operation(
            summary = "Register a new ESAL",
            description = "Register an ESAL with no linked member.",
            tags = "ESAL",
            parameters = {
                    @Parameter(name = "X-XSRF-TOKEN", in = ParameterIn.HEADER, required = true, example = "a6f5086d-af6b-464f-988b-7a604e46062b", description = "For take this value, open your inspector code on your browser, and take the value of the cookie with the name 'XSRF-TOKEN'. Example: a6f5086d-af6b-464f-988b-7a604e46062b"),
                    @Parameter(name = "XSRF-TOKEN", in = ParameterIn.COOKIE,required = true, example = "a6f5086d-af6b-464f-988b-7a604e46062b", description = "Same value of X-XSRF-TOKEN")
            },
            security = {
                    @SecurityRequirement(name = "accessToken")
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not register the ESAL.",
                            content = @Content()
                    )
            }
    )
    @PostMapping("/admin")
    @RolesAllowed("ADMIN")
    @ResponseBody
    public void registerESALAsAdmin(@RequestBody ESALRequestDto dto) {
        registerESALAction.execute(dto);
    }
}
