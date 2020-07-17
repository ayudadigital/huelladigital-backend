package com.huellapositiva.application.controller;

import com.huellapositiva.application.dto.ProposalRequestDto;
import com.huellapositiva.application.dto.ProposalResponseDto;
import com.huellapositiva.application.exception.InvalidJwtTokenException;
import com.huellapositiva.application.exception.ProposalNotPublished;
import com.huellapositiva.domain.actions.FetchProposalAction;
import com.huellapositiva.domain.actions.JoinProposalAction;
import com.huellapositiva.domain.actions.RegisterProposalAction;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/proposals")
public class ProposalApiController {

    private final RegisterProposalAction registerProposalAction;

    private final FetchProposalAction fetchProposalAction;

    private final JoinProposalAction joinProposalAction;

    @PostMapping("/register")
    @ResponseBody
    public void createProposal(@RequestBody ProposalRequestDto dto, HttpServletRequest req) {
        try {
            registerProposalAction.execute(dto, req);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Could not register the proposal");
        }
    }

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

    @PostMapping("/{id}/join")
    @ResponseStatus(HttpStatus.OK)
    public void joinProposal(@PathVariable Integer id, HttpServletRequest req) {
        try {
            joinProposalAction.execute(id, req);
        } catch(EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Proposal with ID " + id + "does not exist");
        }
        catch (ProposalNotPublished e) {
            throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "Proposal is not published yet");
        } catch (InvalidJwtTokenException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "JWT is not valid");
        }
    }
}
