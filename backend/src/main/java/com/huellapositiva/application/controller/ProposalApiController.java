package com.huellapositiva.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.*;
import com.huellapositiva.application.exception.FailedToPersistProposal;
import com.huellapositiva.application.exception.ProposalNotPublished;
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

@RestController
@AllArgsConstructor
@Tag(name = "Proposal Service", description = "The proposals API")
@RequestMapping("/api/v1/proposals")
public class ProposalApiController {

    private final RegisterProposalAction registerProposalAction;

    private final FetchProposalAction fetchProposalAction;

    private final JoinProposalAction joinProposalAction;

    private final FetchPaginatedProposalsAction fetchPaginatedProposalsAction;

    private final RequestProposalRevisionAction requestProposalRevisionAction;

    private final SubmitProposalRevisionAction submitProposalRevisionAction;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Operation(
            summary = "Register a new proposal",
            description = "Register a new proposal and link it to the logged employee.",
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
        dto.setPublished(true);
        try {
            String id = registerProposalAction.execute(dto, file, contactPersonEmail);
            URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}").buildAndExpand(id)
                    .toUri();
            requestProposalRevisionAction.execute(uri);
            res.addHeader(HttpHeaders.LOCATION, uri.toString());
        } catch (ParseException e) {
            throw new FailedToPersistProposal("The given date(s) format is not valid.");
        } catch (IllegalArgumentException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The given category in not valid.");
        } catch (InvalidProposalRequestException e){
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
                            content = @Content()
                    )
            }
    )
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProposalResponseDto getProposal(@PathVariable String id) {
        try {
            return fetchProposalAction.execute(id);
        } catch (EntityNotFoundException | ProposalNotPublished e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Proposal with ID " + id + "does not exist or is not published.");
        }
    }

    @Operation(
            summary = "Join a proposal",
            description = "Join a proposal as volunteer",
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
        } catch (EntityNotFoundException | ProposalNotPublished e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Proposal with ID " + id + " does not exist or is not published.");
        }
    }

    @Operation(
            summary = "Register a new proposal as reviser",
            description = "Register a new proposal by providing the ESAL name through the DTO.",
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
            String id = registerProposalAction.execute(dto, file);
            URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}").buildAndExpand(id)
                    .toUri();
            res.addHeader(HttpHeaders.LOCATION, uri.toString().replace("/reviser", ""));
        } catch (ParseException pe) {
            throw new FailedToPersistProposal("Could not format the following date: " + dto.getClosingProposalDate());
        }
    }

    @Operation(
            summary = "Fetch a list of published proposals",
            description = "Fetch a list of published proposals sorted by the proximity of their closing date",
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
    public ListedProposalsDto fetchListedProposals(@PathVariable Integer page, @PathVariable Integer size) {
        return fetchPaginatedProposalsAction.execute(page, size);
    }

    @PostMapping(path = "/revision/{id}")
    @RolesAllowed("REVISER")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public void submitProposalRevision(@PathVariable String id,
                                       @RequestBody ProposalRevisionDto dto,
                                       @AuthenticationPrincipal String reviserEmail) {
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(id)
                .toUri();
        dto.setReviserEmail(reviserEmail);
        submitProposalRevisionAction.execute(id, dto, uri);
    }
}
