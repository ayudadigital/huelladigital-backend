package com.huellapositiva.application.controller;

import com.huellapositiva.application.dto.ProposalRequestDto;
import com.huellapositiva.domain.actions.RegisterProposalAction;
import com.huellapositiva.domain.service.ProposalService;
import com.huellapositiva.infrastructure.orm.model.Organization;
import com.huellapositiva.infrastructure.orm.repository.JpaOrganizationEmployeeRepository;
import com.huellapositiva.infrastructure.security.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/proposals")
public class ProposalApiController {

    private final RegisterProposalAction registerProposalAction;

    private final ProposalService proposalService;

    @PostMapping("/register")
    @ResponseBody
    public void createProposal(@RequestBody ProposalRequestDto dto, HttpServletRequest req) {
        try {
            registerProposalAction.execute(dto, req);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Could not register the proposal");
        }
    }

//    @GetMapping("/{id}")
//    @ResponseStatus(HttpStatus.OK)
//    public void getProposal(@PathVariable Integer id) {
//        return proposalService.execute(id);
//    }
}
