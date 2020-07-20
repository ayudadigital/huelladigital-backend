package com.huellapositiva.application.controller;

import com.huellapositiva.application.dto.ProposalRequestDto;
import com.huellapositiva.application.dto.ProposalResponseDto;
import com.huellapositiva.application.exception.InvalidJwtTokenException;
import com.huellapositiva.application.exception.ProposalNotPublished;
import com.huellapositiva.application.exception.UserNotConfirmed;
import com.huellapositiva.domain.actions.FetchProposalAction;
import com.huellapositiva.domain.actions.JoinProposalAction;
import com.huellapositiva.domain.actions.RegisterProposalAction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;

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
            description = "Register a new proposal and link it to the logged employee",
            tags = "proposals"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Ok, proposal register successful"
                    ),
                    @ApiResponse(
                            responseCode = "422",
                            description = "Unprocessable entity, in order to create a proposal the employee has to be confirmed.",
                            content = @Content()
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Conflict, could not register proposal.",
                            content = @Content()
                    )
            }
    )
    @PostMapping("/register")
    @RolesAllowed({"ORGANIZATION_EMPLOYEE","ORGANIZATION_EMPLOYEE_NOT_CONFIRMED"})
    @ResponseBody
    public void createProposal(@RequestBody ProposalRequestDto dto, HttpServletRequest req) {
        try {
            registerProposalAction.execute(dto, req);
        } catch (UserNotConfirmed e){
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Employee is not confirmed yet");
        }catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Could not register the proposal");
        }
    }

    @Operation(
            summary = "Fetch a proposal",
            description = "Fetch a proposal with the given ID through the path variable",
            tags = "proposals"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Ok, proposal fetched successfully"
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
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProposalResponseDto getProposal(@PathVariable Integer id) {
        try {
            return fetchProposalAction.execute(id);
        } catch(EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Proposal with ID " + id + "does not exist");
        }
        catch (ProposalNotPublished e) {
            throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "Proposal is not published yet");
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
                            description = "Ok, proposal joined successfully"
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
                    ),
                    @ApiResponse(
                            responseCode = "422",
                            description = "Unprocessable entity, in order to join a proposal, the volunteer has to be confirmed.",
                            content = @Content()
                    )
            }
    )
    @PostMapping("/{id}/join")
    @RolesAllowed({"VOLUNTEER", "VOLUNTEER_NOT_CONFIRMED"})
    @ResponseStatus(HttpStatus.OK)
    public void joinProposal(@PathVariable Integer id, HttpServletRequest req) {
        try {
            joinProposalAction.execute(id, req);
        } catch(EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Proposal with ID " + id + "does not exist");
        } catch (ProposalNotPublished e) {
            throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "Proposal is not published yet");
        } catch (UserNotConfirmed e){
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Volunteer is not confirmed yet");
        } catch (InvalidJwtTokenException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "JWT is not valid");
        }
    }
}
