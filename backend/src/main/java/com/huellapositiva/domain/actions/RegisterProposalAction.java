package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.ProposalRequestDto;
import com.huellapositiva.domain.service.ProposalService;
import com.huellapositiva.infrastructure.orm.entities.Organization;
import com.huellapositiva.infrastructure.orm.repository.JpaOrganizationMemberRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaOrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

@RequiredArgsConstructor
@Service
public class RegisterProposalAction {

    private final JpaOrganizationMemberRepository jpaOrganizationMemberRepository;

    private final JpaOrganizationRepository jpaOrganizationRepository;

    private final ProposalService proposalService;

    public Integer execute(ProposalRequestDto dto, String employeeEmail) {
        Organization organization = jpaOrganizationMemberRepository.findByEmail(employeeEmail)
                .orElseThrow( () -> new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR))
                .getJoinedOrganization();
        dto.setOrganizationName(organization.getName());
        return proposalService.registerProposal(dto);
    }

    public Integer execute(ProposalRequestDto dto) {
        Organization organization = jpaOrganizationRepository.findByName(dto.getOrganizationName())
                .orElseThrow( () -> new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        dto.setOrganizationName(organization.getName());
        return proposalService.registerProposal(dto);
    }
}
