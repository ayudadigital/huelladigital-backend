package com.huellapositiva.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.*;
import com.huellapositiva.application.exception.*;
import com.huellapositiva.domain.actions.*;
import com.huellapositiva.domain.exception.*;
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
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

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

    private final ObjectMapper objectMapper = new ObjectMapper();

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
                            responseCode = "412",
                            description = "Bad request. The ID is not in review pending."
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not fetch the user data due to a connectivity issue."
                    )
            }
    )
    @PostMapping(path = "/revision/{id}")
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
            throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "The ID is not in REVIEW_PENDING.");
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
    @GetMapping("/{idProposal}/volunteers")
    @RolesAllowed({"REVISER", "CONTACT_PERSON"})
    @ResponseStatus(HttpStatus.OK)
    public List<VolunteerDto> fetchListedVolunteersInProposal(@PathVariable String idProposal) {
        try {
            ProposalResponseDto proposalResponseDto = fetchProposalAction.execute(idProposal);
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
                            responseCode = "412",
                            description = "Precondition failed, illegal status."
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
    public void cancelProposalAsReviser(@PathVariable("id") String idProposal,
                                        @RequestBody ProposalCancelReasonDto dto) {
        try {
            cancelProposalAction.executeByReviser(idProposal, dto);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, PROPOSAL_DOESNT_EXIST);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "Status of proposal is not suitable for cancelling");
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

    @PostMapping("/updateProposal")
    @RolesAllowed("CONTACT_PERSON")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateProposal(@Validated @RequestBody UpdateProposalRequestDto updateProposalRequestDto,
                               @Parameter(hidden = true) @AuthenticationPrincipal String accountId){
        try {
            updateProposalAction.execute(updateProposalRequestDto, accountId);
        } catch (ParseException | InvalidProposalCategoryException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (UserNotFoundException | EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (SkillAlreadyExistsException |
                RequirementAlreadyExistsException |
                ProposalNotLinkedWithContactPersonException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }
}