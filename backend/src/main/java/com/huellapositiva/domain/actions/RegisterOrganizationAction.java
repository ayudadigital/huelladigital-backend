package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.OrganizationRequestDto;
import com.huellapositiva.domain.service.OrganizationEmployeeService;
import com.huellapositiva.domain.service.OrganizationService;
import com.huellapositiva.infrastructure.orm.model.OrganizationEmployee;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RegisterOrganizationAction {

    private final OrganizationService organizationService;

    private final OrganizationEmployeeService organizationEmployeeService;

    public void execute(OrganizationRequestDto dto, OrganizationEmployee employee) {
        Integer organizationId = organizationService.create(dto, employee);
        organizationService.findById(organizationId);
        organizationEmployeeService.updateJoinedOrganization(employee, organizationService.findById(organizationId));
    }
}
