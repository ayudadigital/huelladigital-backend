package com.huellapositiva.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.*;
import com.huellapositiva.application.exception.FailedToPersistProposalException;
import com.huellapositiva.application.exception.ProposalNotPublicException;
import com.huellapositiva.application.exception.ProposalNotPublishedException;
import com.huellapositiva.domain.actions.*;
import com.huellapositiva.domain.exception.InvalidProposalRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@Tag(name = "Proposal Service", description = "The proposals API")
@RequestMapping("/api/v1/proposals")
public class ProposalApiController {

    private static final String PATH_ID = "/{id}";

    private static final String PROPOSAL_DOESNT_EXIST = "The given proposal does not exist.";

    private final RegisterProposalAction registerProposalAction;

    private final FetchProposalAction fetchProposalAction;

    private final JoinProposalAction joinProposalAction;

    private final FetchPaginatedProposalsAction fetchPaginatedProposalsAction;

    private final RequestProposalRevisionAction requestProposalRevisionAction;

    private final SubmitProposalRevisionAction submitProposalRevisionAction;

    private final CancelProposalAction cancelProposalAction;

    private final ChangeStatusVolunteerAction changeStatusVolunteerAction;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Operation(
            summary = "Register a new proposal",
            description = "Register a new proposal and link it to the logged employee. This steps require the access token and the XSRF-TOKEN." +
                    "Steps to test in postman: " + "1ª. Register a contact person.  2ª. Login as contact person 3ª. Register an Esal. " +
                    "4ª. Use this method. ",
            tags = "proposals",
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
                            responseCode = "201",
                            description = "Ok, proposal register successful."
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request, a conflict was encountered while attempting to persist the proposal."
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not fetch the user data due to a connectivity issue."
                    )
            }
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RolesAllowed({"CONTACT_PERSON", "CONTACT_PERSON_NOT_CONFIRMED"})
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public void createProposal(@RequestPart("dto") MultipartFile dtoMultipart,
                               @RequestPart("file") MultipartFile file,
                               @AuthenticationPrincipal String contactPersonEmail,
                               HttpServletResponse res) throws IOException {
        ProposalRequestDto dto = objectMapper.readValue(dtoMultipart.getBytes(), ProposalRequestDto.class);
        try {
            String id = registerProposalAction.executeByContactPerson(dto, file, contactPersonEmail);
            URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path(PATH_ID).buildAndExpand(id)
                    .toUri();
            requestProposalRevisionAction.execute(uri);
            res.addHeader(HttpHeaders.LOCATION, uri.toString());
        } catch (ParseException e) {
            throw new FailedToPersistProposalException("The given date(s) format is not valid.");
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The given category in not valid.");
        } catch (InvalidProposalRequestException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Operation(
            summary = "Fetch a proposal",
            description = "Fetch a proposal with the given ID through the path variable. This steps require the access token " +
                    "Steps to test in postman:1ª. Register a contact person.  2ª. Login as contact person 3ª. Register an Esal." +
                    "4ª. Register a new proposal. 5ª. Use this method",
            tags = "proposals"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Ok, proposal fetched successfully."
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Not found, the given ID was not found or is not published.",
                            content = @Content()
                    )
            }
    )
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProposalResponseDto getProposal(@PathVariable String id) {
        try {
            return fetchProposalAction.execute(id);
        } catch (EntityNotFoundException | ProposalNotPublicException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, PROPOSAL_DOESNT_EXIST);
        }
    }

    @Operation(
            summary = "Join a proposal",
            description = "Join a proposal as volunteer. This steps require the access token" +
                    "Steps to test in postman:1ª. Register a contact person.  2ª. Login as contact person 3ª. Register an Esal." +
                    "4ª. Register a new proposal. 5ª. Register a new volunteer. 6ª. Login as volunteer. 7ª. Use this method.",
            tags = "proposals",
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
                            responseCode = "200",
                            description = "Ok, proposal joined successfully."
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Not found, the given ID was not found or is not published.",
                            content = @Content()
                    )
            }
    )
    @PostMapping("/{id}/join")
    @RolesAllowed("VOLUNTEER")
    @ResponseStatus(HttpStatus.OK)
    public void joinProposal(@PathVariable String id, @AuthenticationPrincipal String memberEmail) {
        try {
            joinProposalAction.execute(id, memberEmail);
        } catch (EntityNotFoundException | ProposalNotPublishedException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Proposal with ID " + id + " does not exist or is not published.");
        }
    }

    @Operation(
            summary = "Register a new proposal as reviser",
            description = "Register a new proposal by providing the ESAL name through the DTO. This steps require the access token" +
                    "Steps to test in postman: 1ª. Login as reviser  2ª. Register a new ESAL. 3ª. Use this method.",
            tags = "proposals",
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
                            responseCode = "201",
                            description = "Ok, proposal register successful."
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request, a conflict was encountered while attempting to persist the proposal."
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not fetch the ESAL data due to a connectivity issue."
                    )
            }
    )
    @PostMapping(path = "/reviser", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RolesAllowed("REVISER")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public void createProposalAsReviser(@RequestPart("dto") MultipartFile dtoMultipart,
                                        @RequestPart("file") MultipartFile file,
                                        HttpServletResponse res) throws IOException {
        ProposalRequestDto dto = objectMapper.readValue(dtoMultipart.getBytes(), ProposalRequestDto.class);
        try {
            String id = registerProposalAction.executeByReviser(dto, file);
            URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path(PATH_ID).buildAndExpand(id)
                    .toUri();
            res.addHeader(HttpHeaders.LOCATION, uri.toString().replace("/reviser", ""));
        } catch (ParseException pe) {
            throw new FailedToPersistProposalException("Could not format the following date: " + dto.getClosingProposalDate());
        }
    }

    @Operation(
            summary = "Fetch a list of published proposals",
            description = "Fetch a list of published proposals sorted by the proximity of their closing date.  This steps require the access token" +
                    "Steps to test in postman: 1ª. Register a contact person  2ª. Login as contact person. 3ª. Register a new ESAL." +
                    "4ª. Create a new proposal. 5ª. Use this method.",
            tags = "proposals"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Ok, published proposals fetched successfully."
                    ),
                    @ApiResponse(
                            responseCode = "200",
                            description = "Ok, no published proposals available to fetch, returning an empty list."
                    )
            }
    )
    @GetMapping("/{page}/{size}")
    @ResponseStatus(HttpStatus.OK)
    public ListedProposalsDto fetchListedPublishedProposals(@PathVariable Integer page, @PathVariable Integer size) {
        return fetchPaginatedProposalsAction.execute(page, size);
    }


    @Operation(
            summary = "Fetch list of proposals",
            description = "Fetch a list of proposals based on the page requested. This steps require the access token" +
                    "Steps to test in postman: 1ª. Login as reviser.  2ª. Register a new ESAL. 3ª. Create a new Proposal." +
                    "4ª. Use this method.",
            tags = "proposals",
            parameters = {
                    @Parameter(name = "X-XSRF-TOKEN", in = ParameterIn.HEADER, required = true, example = "3bd06099-6598-4b22-b012-5bfe0701edbe", description = "For taking this value, open your inspector code on your browser, and take the value of the cookie with the name 'XSRF-TOKEN'. Example: a6f5086d-af6b-464f-988b-7a604e46062b"),
                    @Parameter(name = "XSRF-TOKEN", in = ParameterIn.COOKIE, required = true, example = "3bd06099-6598-4b22-b012-5bfe0701edbe", description = "Same value of X-XSRF-TOKEN")
            },
            security = {
                    @SecurityRequirement(name = "accessToken")
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Ok, proposal list fetched."
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request, a conflict was encountered while attempting to persist the proposals."
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not fetch the user data due to a connectivity issue."
                    )
            }
    )
    @RolesAllowed("REVISER")
    @GetMapping("/{page}/{size}/reviser")
    @ResponseStatus(HttpStatus.OK)
    public ListedProposalsDto fetchListedProposals(@PathVariable Integer page, @PathVariable Integer size) {
        return fetchPaginatedProposalsAction.executeAsReviser(page, size);
    }


    @Operation(
            summary = "Submit proposal revision",
            description = "Submit a proposal for revision to the reviser. This steps require the access token and the XSRF-TOKEN." +
                    "Steps to test in postman: 1ª. Register a contact person. 2ª. Login as contact person. " +
                    "3ª. Register a new ESAL. 4ª. Create a proposal.(review_pending)" +
                    "5ª. Login as reviser. 6ª. Use this method.",
            tags = "proposals",
            parameters = {
                    @Parameter(name = "X-XSRF-TOKEN", in = ParameterIn.HEADER, required = true, example = "ff79038b-3fec-41f0-bab8-6e0d11db986e", description = "For taking this value, open your inspector code on your browser, and take the value of the cookie with the name 'XSRF-TOKEN'. Example: a6f5086d-af6b-464f-988b-7a604e46062b"),
                    @Parameter(name = "XSRF-TOKEN", in = ParameterIn.COOKIE, required = true, example = "ff79038b-3fec-41f0-bab8-6e0d11db986e", description = "Same value of X-XSRF-TOKEN")
            },
            security = {
                    @SecurityRequirement(name = "accessToken")
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Ok, email with proposal sent to reviser."
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Not found, requested proposal not found or not published."
                    )
            }
    )
    @PostMapping(path = "/revision/{id}")
    @RolesAllowed("REVISER")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public void submitProposalRevision(@PathVariable String id,
                                       @RequestBody ProposalRevisionDto dto,
                                       @AuthenticationPrincipal String reviserEmail) {
        try {
            URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path(PATH_ID).buildAndExpand(id)
                    .toUri();
            dto.setReviserEmail(reviserEmail);
            submitProposalRevisionAction.execute(id, dto, uri);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, PROPOSAL_DOESNT_EXIST);
        }
    }


    @Operation(
            summary = "Fetch list of volunteers in a proposal",
            description = "Fetch list of volunteers in a proposal by the reviser. This steps require the access token" +
                    "Steps to test in postman: 1ª. Register a contact person. 2ª. Login as contact person. " +
                    "3ª. Register a new ESAL. 4ª. Create a proposal." +
                    "5ª. Register a new Volunteer. 6ª. Login as volunteer. 7ª. Join the proposal. " +
                    "8ª. Login as reviser or contact person. 9ª. Use this method.",
            tags = {"proposals, volunteers"},
            parameters = {
                    @Parameter(name = "X-XSRF-TOKEN", in = ParameterIn.HEADER, required = true, example = "ff79038b-3fec-41f0-bab8-6e0d11db986e", description = "For taking this value, open your inspector code on your browser, and take the value of the cookie with the name 'XSRF-TOKEN'. Example: a6f5086d-af6b-464f-988b-7a604e46062b"),
                    @Parameter(name = "XSRF-TOKEN", in = ParameterIn.COOKIE, required = true, example = "ff79038b-3fec-41f0-bab8-6e0d11db986e", description = "Same value of X-XSRF-TOKEN")
            },
            security = {
                    @SecurityRequirement(name = "accessToken")
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Ok, list of volunteers fetched."
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Requested proposal not found."
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not fetch the user data due to a connectivity issue."
                    )
            }
    )
    @GetMapping("/{idProposal}/volunteers")
    @RolesAllowed({"REVISER", "CONTACT_PERSON"})
    @ResponseStatus(HttpStatus.OK)
    public List<VolunteerDto> fetchListedVolunteersInProposal(@PathVariable String idProposal) {
        try {
            ProposalResponseDto proposalResponseDto = fetchProposalAction.execute(idProposal);
            return proposalResponseDto.getInscribedVolunteers();
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, PROPOSAL_DOESNT_EXIST);
        }
    }

    @Operation(
            summary = "Fetch a proposal with the list of volunteers",
            description = "Fetch a proposal with the list of volunteers by the reviser. This steps require the access token." +
                    "Steps to test in postman: 1ª. Register a contact person. 2ª. Login as contact person. " +
                    "3ª. Register a new ESAL. 4ª. Create a proposal." +
                    "5ª. Register a new Volunteer. 6ª. Login as volunteer. 7ª. Join the proposal." +
                    "8ª. Login as reviser or contact person. 9ª. Use this method.",
            tags = {"proposals, volunteers"},
            parameters = {
                    @Parameter(name = "X-XSRF-TOKEN", in = ParameterIn.HEADER, required = true, example = "ff79038b-3fec-41f0-bab8-6e0d11db986e", description = "For taking this value, open your inspector code on your browser, and take the value of the cookie with the name 'XSRF-TOKEN'. Example: a6f5086d-af6b-464f-988b-7a604e46062b"),
                    @Parameter(name = "XSRF-TOKEN", in = ParameterIn.COOKIE, required = true, example = "ff79038b-3fec-41f0-bab8-6e0d11db986e", description = "Same value of X-XSRF-TOKEN")
            },
            security = {
                    @SecurityRequirement(name = "accessToken")
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Ok, proposal fetched successfully and listed the list of volunteers."
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Requested proposal not found."
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not fetch the user data due to a connectivity issue."
                    )
            }
    )
    @GetMapping("/{idProposal}/proposal")
    @RolesAllowed({"REVISER", "CONTACT_PERSON"})
    @ResponseStatus(HttpStatus.OK)
    public ProposalResponseDto fetchProposalWithVolunteers(@PathVariable String idProposal) {
        try {
            return fetchProposalAction.execute(idProposal);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, PROPOSAL_DOESNT_EXIST);
        }
    }

    @Operation(
            summary = "Cancel a proposal",
            description = "Changes ProposalStatus to CANCELLED. Only Reviser is allowed to do it. This steps require the access token and the XSRF-TOKEN." +
                    "Steps to test in postman: 1ª. Register a contact person. 2ª. Login as contact person." +
                    "3ª. Register a new ESAL.  4ª. Create a proposal. 5ª. Login as reviser. 6ª. Use this method.",
            tags = "proposals",
            parameters = {
                    @Parameter(name = "X-XSRF-TOKEN", in = ParameterIn.HEADER, required = true, example = "ff79038b-3fec-41f0-bab8-6e0d11db986e", description = "For taking this value, open your inspector code on your browser, and take the value of the cookie with the name 'XSRF-TOKEN'. Example: a6f5086d-af6b-464f-988b-7a604e46062b"),
                    @Parameter(name = "XSRF-TOKEN", in = ParameterIn.COOKIE, required = true, example = "ff79038b-3fec-41f0-bab8-6e0d11db986e", description = "Same value of X-XSRF-TOKEN")
            },
            security = {
                    @SecurityRequirement(name = "accessToken")
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "No Content, proposal status changed to CANCELLED successfully."
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Requested proposal not found."
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not fetch the user data due to a connectivity issue."
                    )
            }
    )
    @PostMapping("/{id}/cancel")
    @RolesAllowed("REVISER")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelProposalAsReviser(@PathVariable("id") String idProposal) {
        try {
            cancelProposalAction.executeByReviser(idProposal);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, PROPOSAL_DOESNT_EXIST);
        }
    }

    @Operation(
            summary = "Change status of the volunteer in proposal",
            description = "The contact person can to change the status of volunteer in a proposal to CONFIRMED/REJECTED." +
                    "We do not send an email in MVP version, it will be added in future versions." +
                    "This method is POST, don´t forget to use the access token as Bearer Token and use the XSRF-TOKEN, copy and paste in HEADER as X-XSRF-TOKEN." +
                    "Steps to use this endpoint:" +
                    "1º. Register a contact person." +
                    "2º. Login with contact person." +
                    "3º. Register ESAL." +
                    "4º. Register a proposal." +
                    "5º. Register a volunteer." +
                    "6º. Login with volunteer." +
                    "7º. Join volunteer in the proposal." +
                    "8º. Login with contact person." +
                    "9º. Use this method.",
            tags = {"proposals, volunteers, contact person"},
            parameters = {
                    @Parameter(name = "X-XSRF-TOKEN", in = ParameterIn.HEADER, required = true, example = "ff79038b-3fec-41f0-bab8-6e0d11db986e", description = "For taking this value, open your inspector code on your browser, and take the value of the cookie with the name 'XSRF-TOKEN'. Example: a6f5086d-af6b-464f-988b-7a604e46062b"),
                    @Parameter(name = "XSRF-TOKEN", in = ParameterIn.COOKIE, required = true, example = "ff79038b-3fec-41f0-bab8-6e0d11db986e", description = "Same value of X-XSRF-TOKEN")
            },
            security = {
                    @SecurityRequirement(name = "accessToken")
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "No Content, proposal status changed to CANCELLED successfully."
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not fetch the user data due to a connectivity issue."
                    )
            }
    )
    @PostMapping("/changeStatusVolunteerProposal")
    @RolesAllowed("CONTACT_PERSON")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeStatusVolunteerInProposal(@RequestBody List<ChangeStatusVolunteerDto> changeStatusVolunteerDtos) {
        changeStatusVolunteerAction.execute(changeStatusVolunteerDtos);
    }
}