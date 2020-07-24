package com.huellapositiva.application.controller;

import com.huellapositiva.application.dto.ProposalRequestDto;
import com.huellapositiva.application.dto.ProposalResponseDto;
import com.huellapositiva.application.exception.ProposalNotPublished;
import com.huellapositiva.domain.actions.FetchProposalAction;
import com.huellapositiva.domain.actions.JoinProposalAction;
import com.huellapositiva.domain.actions.RegisterProposalAction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;

@RestController
@AllArgsConstructor
@Tag(name = "Proposal Service", description = "The proposals API")
@RequestMapping("/api/v1/proposals")
public class ProposalApiController {

    private final RegisterProposalAction registerProposalAction;

    private final FetchProposalAction fetchProposalAction;

    private final JoinProposalAction joinProposalAction;

    @Operation(
            summary = "Register a new proposal",
            description = "Register a new proposal and link it to the logged employee.",
            tags = "proposals"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Ok, proposal register successful."
                    )
            }
    )
    @PostMapping
    @RolesAllowed("ORGANIZATION_MEMBER")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public void createProposal(@RequestBody ProposalRequestDto dto, @AuthenticationPrincipal String memberEmail, HttpServletResponse res) {
        dto.setPublished(true);
        int id = registerProposalAction.execute(dto, memberEmail);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(id)
                .toUri();
        res.addHeader(HttpHeaders.LOCATION, uri.toString());
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
                            description = "Not found, the given ID was not found.",
                            content = @Content()
                    )
            }
    )
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProposalResponseDto getProposal(@PathVariable Integer id) {
        try {
            return fetchProposalAction.execute(id);
        } catch(EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Proposal with ID " + id + "does not exist.");
        }
        catch (ProposalNotPublished e) {
            throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "Proposal is not published yet.");
        }
    }

    @Operation(
            summary = "Join a proposal",
            description = "Join a proposal as volunteer",
            tags = "proposals"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Ok, proposal joined successfully."
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Not found, the given ID was not found.",
                            content = @Content()
                    ),
                    @ApiResponse(
                            responseCode = "412",
                            description = "Precondition failed, the proposal you are looking for is not published yet.",
                            content = @Content()
                    )
            }
    )
    @PostMapping("/{id}/join")
    @RolesAllowed("VOLUNTEER")
    @ResponseStatus(HttpStatus.OK)
    public void joinProposal(@PathVariable Integer id, @AuthenticationPrincipal String memberEmail) {
        try {
            joinProposalAction.execute(id, memberEmail);
        } catch(EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Proposal with ID " + id + "does not exist.");
        } catch (ProposalNotPublished e) {
            throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "Proposal is not published yet.");
        }
    }
}
