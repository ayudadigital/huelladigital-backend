package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.ProposalRequestDto;
import com.huellapositiva.domain.service.ProposalService;
import com.huellapositiva.infrastructure.orm.model.Organization;
import com.huellapositiva.infrastructure.orm.repository.JpaOrganizationEmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

@RequiredArgsConstructor
@Service
public class RegisterProposalAction {

    private final JpaOrganizationEmployeeRepository jpaOrganizationEmployeeRepository;

    private final ProposalService proposalService;

    public void execute(ProposalRequestDto dto) {
        String employeeEmail = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Organization organization = jpaOrganizationEmployeeRepository.findByEmail(employeeEmail)
                .orElseThrow( () -> new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR))
                .getJoinedOrganization();
        dto.setOrganization(organization);
        proposalService.registerProposal(dto);
    }
}
