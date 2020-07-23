package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.OrganizationRequestDto;
import com.huellapositiva.domain.model.entities.User;
import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.entities.Organization;
import com.huellapositiva.domain.repository.OrganizationRepository;
import com.huellapositiva.domain.service.OrganizationMemberService;
import com.huellapositiva.domain.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RegisterOrganizationAction {

    private final OrganizationService organizationService;

    private final OrganizationMemberService organizationMemberService;

    private final OrganizationRepository organizationRepository;

    public void execute(OrganizationRequestDto dto, EmailAddress memberEmail) {
        Organization organization = new Organization(dto.getName());
        User user = organizationMemberService.fetch(memberEmail);
        organization.addUserAsMember(user);
        organizationRepository.save(organization);
    }

    public void execute(OrganizationRequestDto dto) {
        organizationService.create(dto);
    }
}
