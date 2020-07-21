package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.OrganizationRequestDto;
import com.huellapositiva.application.exception.UserNotFound;
import com.huellapositiva.domain.service.OrganizationEmployeeService;
import com.huellapositiva.domain.service.OrganizationService;
import com.huellapositiva.infrastructure.orm.model.OrganizationEmployee;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RegisterOrganizationAction {

    private final OrganizationService organizationService;

    private final OrganizationEmployeeService organizationEmployeeService;

    public void execute(OrganizationRequestDto dto) {
        String employeeEmail = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        OrganizationEmployee employee = organizationEmployeeService.findByEmail(employeeEmail)
                .orElseThrow( () -> new UserNotFound("Could not retrieve the organization employee by his email."));

        Integer organizationId = organizationService.create(dto, employee);
        organizationService.findById(organizationId);
        organizationEmployeeService.updateJoinedOrganization(employee, organizationService.findById(organizationId));
    }
}
