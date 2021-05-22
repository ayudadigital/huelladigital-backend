package com.huellapositiva.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.*;
import com.huellapositiva.application.exception.*;
import com.huellapositiva.domain.actions.*;
import com.huellapositiva.domain.exception.*;
import com.huellapositiva.domain.model.valueobjects.Roles;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.nio.file.AccessDeniedException;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

import static com.huellapositiva.domain.model.valueobjects.Roles.REVISER;
import static com.huellapositiva.domain.util.StringUtils.maskEmailAddress;

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

    private final UpdateProposalAction updateProposalAction;

    private final CloseProposalEnrollmentAction closeProposalEnrollmentAction;

    private final PublishProposalAction publishProposalAction;

    private final UpdateProposalImageAction updateProposalImageAction;

    private final ChangeProposalStatusToFinishedAction changeProposalStatusToFinishedAction;

    private ChangeStatusToInadequateAction changeStatusToInadequateAction;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Operation(
            summary = "Change proposal status to FINISHED",
            description = "Changes ProposalStatus to FINISHED only if current status is PUBLISHED or ENROLMENT_CLOSED. Only Contact_Person is allowed to do it. Roles allowed CONTACT_PERSON.",
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
                            description = "Proposal status changed to FINISHED"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Requested proposal not found"
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Conflict, current status is illegal."
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not fetch the user data due to a connectivity issue."
                    ),
            }
    )
    @PutMapping("/{id}/status/finished")
    @RolesAllowed("CONTACT_PERSON")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeProposalStatusToFinishedAction(@PathVariable("id") String proposalId){
        try{
            changeProposalStatusToFinishedAction.execute(proposalId);
        }catch (EntityNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, PROPOSAL_DOESNT_EXIST);
        }catch (InvalidProposalStatusException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Unable to set proposal status to Finished due to current status");
        }
    }

    @Operation(
            summary = "Register a new proposal",
            description = "Register a new proposal and link it to the logged employee. " +
                    "Roles allowed CONTACT_PERSON and CONTACT_PERSON_NOT_CONFIRMED",
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
                               @AuthenticationPrincipal String accountId,
                               HttpServletResponse res) throws IOException {
        ProposalRequestDto dto = objectMapper.readValue(dtoMultipart.getBytes(), ProposalRequestDto.class);
        try {
            String id = registerProposalAction.executeByContactPerson(dto, file, accountId);
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
            description = "Fetch a proposal with the given ID through the path variable.",
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
                            content = @Content(mediaType = "application/json")
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
            description = "Join a proposal as volunteer. Roles allowed VOLUNTEER",
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
                            description = "Not found, the given ID was not found or is not published."
                    ),
                    @ApiResponse(
                            responseCode = "410",
                            description = "Gone, the proposal is already closed."
                    )
            }
    )
    @PostMapping("/{id}/join")
    @RolesAllowed("VOLUNTEER")
    @ResponseStatus(HttpStatus.OK)
    public void joinProposal(@Schema(description = "Id of the proposal", example = "00000000-0000-0000-0000-000000000000") @PathVariable String id,
                             @Parameter(hidden = true) @AuthenticationPrincipal String accountId) {
        try {
            joinProposalAction.execute(id, accountId);
        } catch (EntityNotFoundException | ProposalNotPublishedException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Proposal with ID " + id + " does not exist or is not published.");
        }
    }

    @Operation(
            summary = "Register a new proposal as reviser",
            description = "Register a new proposal by providing the ESAL name through the DTO. Roles allowed REVISER",
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
            description = "Fetch a list of published proposals sorted by the proximity of their closing date.",
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
            description = "Fetch a list of proposals based on the page requested. Roles allowed REVISER",
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
                            description = "Bad request, a conflict was encountered while attempting to persist the proposals.",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not fetch the user data due to a connectivity issue.",
                            content = @Content(mediaType = "application/json")
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
            description = "Submit a proposal for revision to the reviser. Roles allowed REVISER",
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
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Conflict. The ID is not in review pending."
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not fetch the user data due to a connectivity issue."
                    )
            }
    )
    @PostMapping(path = "/{id}/revision")
    @RolesAllowed("REVISER")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public void submitProposalRevision(@PathVariable String id,
                                       @RequestBody ProposalRevisionDto dto,
                                       @Parameter(hidden = true) @AuthenticationPrincipal String accountId) {
        try {
            URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path(PATH_ID).buildAndExpand(id)
                    .toUri();
            submitProposalRevisionAction.execute(id, dto, uri, accountId);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, PROPOSAL_DOESNT_EXIST);
        } catch (InvalidProposalStatusException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage());
        }
    }

    @Operation(
            summary = "Fetch list of volunteers in a proposal",
            description = "Fetch list of volunteers in a proposal by the reviser. Roles allowed REVISER and CONTACT_PERSON",
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
                            description = "Requested proposal not found.",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not fetch the user data due to a connectivity issue.",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    @GetMapping("/{id}/volunteers")
    @RolesAllowed({"REVISER", "CONTACT_PERSON"})
    @ResponseStatus(HttpStatus.OK)
    public List<VolunteerDto> fetchListedVolunteersInProposal(@PathVariable("id") String proposalId) {
        try {
            ProposalResponseDto proposalResponseDto = fetchProposalAction.execute(proposalId);
            return proposalResponseDto.getInscribedVolunteers()
                    .stream()
                    .map(v -> new VolunteerDto(v.getId(), maskEmailAddress(v.getEmailAddress()), v.getConfirmed()))
                    .collect(Collectors.toList());
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, PROPOSAL_DOESNT_EXIST);
        }
    }

    @Operation(
            summary = "Fetch a proposal with the list of volunteers",
            description = "Fetch a proposal with the list of volunteers by the reviser. Roles allowed REVISER, CONTACT_PERSON",
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
                            description = "Requested proposal not found.",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not fetch the user data due to a connectivity issue.",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    @GetMapping("/{id}/proposal")
    @RolesAllowed({"REVISER", "CONTACT_PERSON"})
    @ResponseStatus(HttpStatus.OK)
    public ProposalResponseDto fetchProposalWithVolunteers(@PathVariable("id") String proposalId) {
        try {
            return fetchProposalAction.execute(proposalId);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, PROPOSAL_DOESNT_EXIST);
        }
    }

    @Operation(
            summary = "Cancel a proposal",
            description = "Changes ProposalStatus to CANCELLED. Only Reviser is allowed to do it. Roles allowed REVISER.",
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
                            responseCode = "409",
                            description = "Precondition failed, illegal status."
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not fetch the user data due to a connectivity issue."
                    )
            }
    )
    @PostMapping("/{id}/status/cancel")
    @RolesAllowed("REVISER")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelProposalAsReviser(@PathVariable("id") String proposalId,
                                        @RequestBody ProposalCancelReasonDto dto) {
        try {
            cancelProposalAction.executeByReviser(proposalId, dto);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, PROPOSAL_DOESNT_EXIST);
        } catch (InvalidProposalStatusException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Status of proposal is not suitable for cancelling");
        }
    }

    @Operation(
            summary = "Change status of the volunteer in proposal",
            description = "The contact person can to change the status of volunteer in a proposal to CONFIRMED/REJECTED. Roles allowed CONTACT_PERSON.",
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

    @Operation(
            summary = "Updates the proposal status to ENROLLMENT_CLOSED",
            description = "The contact person can update the proposal status from PUBLISHED to ENROLLMENT_CLOSE.",
            tags = {"proposals, reviser"},
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
                            description = "No Content, proposal status changed to ENROLLMENT_CLOSE successfully."
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "The proposal status in database is not PUBLISHED."
                    )
            }
    )
    @PutMapping("{id}/status/close")
    @RolesAllowed("CONTACT_PERSON")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void closeEnrollment(@PathVariable("id") String proposalId) {
        closeProposalEnrollmentAction.execute(proposalId);
    }

    @Operation(
            summary = "Updates the proposal status to PUBLISHED",
            description = "The reviser can update the proposal status from REVIEW_PENDING and ENROLLMENT_CLOSE to PUBLISHED.",
            tags = {"proposals, reviser"},
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
                            description = "No Content, proposal status changed to PUBLISHED successfully."
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Conflict, The proposal status in database is not REVIEW_PENDING or ENROLLMENT_CLOSE."
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not fetch the user data due to a connectivity issue."
                    )
            }
    )
    @PutMapping("/{id}/status/publish")
    @RolesAllowed({"REVISER", "CONTACT_PERSON"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void publishProposal(@PathVariable("id") String proposalId,
                                Authentication authentication) {
        List<Roles> roles = authentication.getAuthorities().stream()
                .map(grantedAuthority -> Roles.valueOf(grantedAuthority.getAuthority().replace("ROLE_", "")))
                .collect(Collectors.toList());

        if (roles.contains(REVISER)) {
            publishProposalAction.executeAsReviser(proposalId);
            return;
        }

        publishProposalAction.executeAsContactPerson(proposalId);
    }

    @Operation(
            summary = "Updates the proposal image",
            description = "The contact person can update the image of the proposal.",
            tags = {"proposals, contact person"},
            parameters = {
                    @Parameter(name = "X-XSRF-TOKEN", in = ParameterIn.QUERY, required = true, example = "ff79038b-3fec-41f0-bab8-6e0d11db986e", description = "For taking this value, open your inspector code on your browser, and take the value of the cookie with the name 'XSRF-TOKEN'. Example: a6f5086d-af6b-464f-988b-7a604e46062b"),
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
                            description = "No Content, proposal status changed to PUBLISHED successfully."
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad Request, The proposal status or image not valid"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden, The contact person related to this proposal does not match the logged contact person.."
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not fetch the user data due to a connectivity issue."
                    )
            }
    )
    @PutMapping("/{proposalId}/image")
    @RolesAllowed("CONTACT_PERSON")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateProposalImage(@RequestPart("photo") MultipartFile photo, @PathVariable String proposalId,
                                  @Parameter(hidden = true) @AuthenticationPrincipal String accountId) throws IOException {
        try {
            updateProposalImageAction.execute(photo, accountId, proposalId);
        } catch(AccessDeniedException ex){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch(IllegalStateException ex){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status of proposal is not suitable for changing image");
        } catch (InvalidFieldException | EmptyFileException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @Operation(
            summary = "Change status of the proposal to Inadequate",
            description = "The reviser decides to change the status of the proposal from review pending to inadequate after the revision",
            tags = {"proposals, reviser, contact person"},
            parameters = {
                    @Parameter(name = "X-XSRF-TOKEN", in = ParameterIn.QUERY, required = true, example = "ff79038b-3fec-41f0-bab8-6e0d11db986e", description = "For taking this value, open your inspector code on your browser, and take the value of the cookie with the name 'XSRF-TOKEN'. Example: a6f5086d-af6b-464f-988b-7a604e46062b"),
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
                            description = "No Content, proposal status changed to INADEQUATE successfully."
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Not Found, the proposal with the given id was not found in the database."
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Conflict, changing the proposal status failed because the proposal was not in review pending."
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not fetch the user data due to a connectivity issue."
                    )
            }
    )
    @PutMapping("/{id}/status/inadequate")
    @RolesAllowed("REVISER")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeStatusToInadequate(@PathVariable("id") String proposalId,
                                         @RequestBody ChangeToInadequateDto dto,
                                         @Parameter(hidden = true) @AuthenticationPrincipal String accountId) {
        try {
            changeStatusToInadequateAction.execute(dto, proposalId);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, PROPOSAL_DOESNT_EXIST);
        } catch (InvalidProposalStatusException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage());
        }
    }

    @Operation(
            summary = "Update proposal",
            description = "The contact person can to update the proposal and this change the status to REVIEW_PENDING. Roles allowed CONTACT_PERSON.",
            tags = {"proposals, contact person"},
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
                            description = "No Content, the proposal has been modified."
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request, some field has wrong format."
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Not found, the contact person not found in the database or the proposal not found in the database."
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Conflict, the proposal is not linked with the contact person, or some skill/requirement is duplicated."
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not fetch the user data due to a connectivity issue."
                    )
            }
    )
    @PutMapping("/{id}")
    @RolesAllowed({"CONTACT_PERSON"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateProposal(@PathVariable("id") String proposalId,
                               @Validated @RequestBody UpdateProposalRequestDto updateProposalRequestDto,
                               @Parameter(hidden = true) @AuthenticationPrincipal String accountId){
        try {
            updateProposalAction.execute(proposalId, updateProposalRequestDto, accountId);
        } catch (ParseException | InvalidFieldException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (ProposalNotLinkedWithContactPersonException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (UserNotFoundException | EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (SkillAlreadyExistsException |
                RequirementAlreadyExistsException |
                InvalidProposalStatusException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }
}

