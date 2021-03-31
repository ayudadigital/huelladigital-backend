package com.huellapositiva.application.controller;

import com.huellapositiva.application.dto.ESALRequestDto;
import com.huellapositiva.application.dto.GetESALResponseDto;
import com.huellapositiva.application.dto.UpdateESALDto;
import com.huellapositiva.application.exception.ESALAlreadyExistsException;
import com.huellapositiva.application.exception.InvalidFieldException;
import com.huellapositiva.application.exception.UserNotFoundException;
import com.huellapositiva.domain.actions.*;
import com.huellapositiva.domain.exception.EmptyFileException;
import com.huellapositiva.domain.exception.UserAlreadyHasESALException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;

@Slf4j
@RestController
@AllArgsConstructor
@Tag(name = "ESAL", description = "The ESAL API.")
@RequestMapping("/api/v1/esal")
public class ESALApiController {

    private final RegisterESALAction registerESALAction;

    private final UploadLogoAction uploadLogoAction;

    private final DeleteESALAction deleteESALAction;

    private final UpdateESALAction updateESALAction;

    private final FetchESALAction fetchESALAction;

    @Operation(
            summary = "Register a new ESAL",
            description = "Register a new ESAL and link it to the logged employee. Roles allowed CONTACT_PERSON and CONTACT_PERSON_NOT_CONFIRMED.",
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
                            responseCode = "400",
                            description = "Bad request, the provided field is not valid.",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Conflict, the provided name is already taken.",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "412",
                            description = "Precondition failed, the user attempting to create the ESAL has another one linked.",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not register the ESAL.",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    @PostMapping
    @RolesAllowed({"CONTACT_PERSON", "CONTACT_PERSON_NOT_CONFIRMED"})
    @ResponseBody
    public void registerESAL(@Validated @RequestBody ESALRequestDto dto,
                             @Parameter(hidden = true) @AuthenticationPrincipal String accountId) {
        try {
            registerESALAction.execute(dto, accountId);
        } catch (ESALAlreadyExistsException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "ESAL named " + dto.getName() + " already exists.");
        } catch (UserAlreadyHasESALException ex) {
            throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "The user attempting to create the ESAL has already registered another one.");
        } catch (UserNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not register the user caused by a connectivity issue");
        }
    }



    @Operation(
            summary = "Return esal information",
            description = "Return esal profile information",
            tags = "ESAL",
            parameters = {
                    @Parameter(name = "X-XSRF-TOKEN", in = ParameterIn.QUERY, required = true, example = "a6f5086d-af6b-464f-988b-7a604e46062b", description = "For take this value, open your inspector code on your browser, and take the value of the cookie with the name 'XSRF-TOKEN'. Example: a6f5086d-af6b-464f-988b-7a604e46062b"),
                    @Parameter(name = "XSRF-TOKEN", in = ParameterIn.COOKIE, required = true, example = "a6f5086d-af6b-464f-988b-7a604e46062b", description = "Same value of X-XSRF-TOKEN")
            },
            security = {
                    @SecurityRequirement(name = "accessToken")
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Ok, return full esal information",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = GetESALResponseDto.class)
                                    )
                            }
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request, credentials are not valid",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden, logged user has no access to that ESAL",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not fetch the esal data due to a connectivity issue.",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    @GetMapping("/{id}")
    @RolesAllowed({"CONTACT_PERSON", "CONTACT_PERSON_NOT_CONFIRMED"})
    @ResponseStatus(HttpStatus.OK)
    public GetESALResponseDto fetchESAL(@PathVariable String id,
                                        @Parameter(hidden = true) @AuthenticationPrincipal String accountId){
        try {
            return fetchESALAction.executeAsOwner(id, accountId);
        } catch (IllegalAccessException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    @Operation(
            summary = "Updates the information of an ESAL",
            description = "Updates the information of the ESAL linked to the logged employee. Roles allowed CONTACT_PERSON and CONTACT_PERSON_NOT_CONFIRMED.",
            tags = "ESAL",
            parameters = {
                    @Parameter(name = "X-XSRF-TOKEN", in = ParameterIn.QUERY, required = true, example = "a6f5086d-af6b-464f-988b-7a604e46062b", description = "For take this value, open your inspector code on your browser, and take the value of the cookie with the name 'XSRF-TOKEN'. Example: a6f5086d-af6b-464f-988b-7a604e46062b"),
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
                            description = "Ok, ESAL updated successfully."
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad Request, unable to validate provided data."
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Not found, user not found in db."
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Conflict, the provided name is already taken."
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not update the esal.",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    @PutMapping("/update")
    @RolesAllowed({"CONTACT_PERSON", "CONTACT_PERSON_NOT_CONFIRMED"})
    public void updateESAL(@Validated @RequestBody UpdateESALDto dto,
                           @Parameter(hidden = true) @AuthenticationPrincipal String accountId){
        try{
            updateESALAction.execute(dto, accountId);
        } catch (DataIntegrityViolationException ex){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "ESAL named " + dto.getName() + " already exists.");
        } catch (UserNotFoundException ex){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find the user due to a connectivity issue.");
        }
    }

    @Operation(
            summary = "Upload ESAL logo",
            description = ". Roles allowed CONTACT_PERSON and CONTACT_PERSON_NOT_CONFIRMED.",
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
                            responseCode = "204",
                            description = "No content, logo uploaded successfully."
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request, logo is not valid"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not upload the logo.",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    @PostMapping(path = "/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RolesAllowed({"CONTACT_PERSON", "CONTACT_PERSON_NOT_CONFIRMED"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void uploadLogo(@RequestPart("logo")MultipartFile logo,
                           @Parameter(hidden = true) @AuthenticationPrincipal String accountId) throws IOException {
        try {
            uploadLogoAction.execute(logo, accountId);
        } catch (InvalidFieldException | EmptyFileException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

    }

    @Operation(
            summary = "Delete an ESAL",
            description = "Delete an ESAL and unlink their members, including their contact person. Roles allowed CONTACT_PERSON.",
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
                            responseCode = "403",
                            description = "Forbidden, the user has no permissions to delete the ESAL.",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not fetch the user data due to a connectivity issue."
                    )
            }
    )
    @DeleteMapping("/{id}")
    @RolesAllowed("CONTACT_PERSON")
    @ResponseBody
    public void deleteESAL(@PathVariable String id,
                           @Parameter(hidden = true) @AuthenticationPrincipal String accountId) {
        deleteESALAction.execute(accountId, id);
    }

    @Operation(
            summary = "Register a new ESAL as reviser",
            description = "Register an ESAL as reviser with no linked member. Roles allowed REVISER.",
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
                            responseCode = "409",
                            description = "ESAL already exists.",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not fetch the user data due to a connectivity issue."
                    )
            }
    )
    @PostMapping("/reviser")
    @RolesAllowed("REVISER")
    @ResponseBody
    public void registerESALAsReviser(@RequestBody ESALRequestDto dto) {
        registerESALAction.execute(dto);
    }
}
