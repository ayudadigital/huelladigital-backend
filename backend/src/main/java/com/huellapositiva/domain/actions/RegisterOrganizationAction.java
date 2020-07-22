package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.OrganizationRequestDto;
import com.huellapositiva.application.exception.UserNotFound;
import com.huellapositiva.domain.service.OrganizationMemberService;
import com.huellapositiva.domain.service.OrganizationService;
import com.huellapositiva.infrastructure.orm.model.OrganizationMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RegisterOrganizationAction {

    private final OrganizationService organizationService;

    private final OrganizationMemberService organizationMemberService;

    public void execute(OrganizationRequestDto dto, String employeeEmail) {
                OrganizationMember employee = organizationMemberService.findByEmail(employeeEmail)
                .orElseThrow( () -> new UserNotFound("Could not retrieve the organization employee by his email."));

        Integer organizationId = organizationService.create(dto, employee);
        organizationService.findById(organizationId);
        organizationMemberService.updateJoinedOrganization(employee, organizationService.findById(organizationId));
    }
}
