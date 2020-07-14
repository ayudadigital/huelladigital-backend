package com.huellapositiva.application.controller;

import com.huellapositiva.application.dto.OrganizationRequestDto;
import com.huellapositiva.application.dto.ProposalRequestDto;
import com.huellapositiva.domain.actions.RegisterProposalAction;
import com.huellapositiva.domain.service.OrganizationService;
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

    private final JwtService jwtService;

    private final OrganizationService organizationService;

    private final JpaOrganizationEmployeeRepository jpaOrganizationEmployeeRepository;

    private final RegisterProposalAction registerProposalAction;

    @PostMapping("/create")
    @ResponseBody
    public void createProposal(@RequestBody ProposalRequestDto dto, HttpServletRequest req) {
        try {
            String authHeader = req.getHeader(HttpHeaders.AUTHORIZATION);
            String employeeEmail = jwtService.getUserDetails(authHeader.replace("Bearer ", "")).getFirst();
            Organization organization = jpaOrganizationEmployeeRepository.findByEmail(employeeEmail)
                    .orElseThrow( () -> new RuntimeException("Could not retrieve the organization employee by his email."))
                    .getJoinedOrganization();
            dto.setOrganization(organization);
            registerProposalAction.execute(dto);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Could not register the user");
        }
    }
}
