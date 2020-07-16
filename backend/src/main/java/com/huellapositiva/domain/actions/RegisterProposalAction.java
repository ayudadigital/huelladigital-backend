package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.ProposalRequestDto;
import com.huellapositiva.application.exception.InvalidJwtTokenException;
import com.huellapositiva.domain.service.ProposalService;
import com.huellapositiva.infrastructure.orm.model.Organization;
import com.huellapositiva.infrastructure.orm.repository.JpaOrganizationEmployeeRepository;
import com.huellapositiva.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@RequiredArgsConstructor
@Service
public class RegisterProposalAction {
    private final JwtService jwtService;

    private final JpaOrganizationEmployeeRepository jpaOrganizationEmployeeRepository;

    private final ProposalService proposalService;

    public void execute(ProposalRequestDto dto, HttpServletRequest req) throws InvalidJwtTokenException {
        String authHeader = req.getHeader(HttpHeaders.AUTHORIZATION);
        String employeeEmail = jwtService.getUserDetails(authHeader.replace("Bearer ", "")).getFirst();
        Organization organization = jpaOrganizationEmployeeRepository.findByEmail(employeeEmail)
                .orElseThrow( () -> new RuntimeException("Could not retrieve the organization employee by his email."))
                .getJoinedOrganization();
        dto.setOrganization(organization);
        proposalService.registerProposal(dto);
    }
}
