package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.ESALRequestDto;
import com.huellapositiva.application.exception.ESALAlreadyExists;
import com.huellapositiva.domain.model.entities.ContactPerson;
import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.entities.Organization;
import com.huellapositiva.domain.repository.OrganizationRepository;
import com.huellapositiva.domain.service.OrganizationMemberService;
import com.huellapositiva.domain.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RegisterESALAction {

    private final OrganizationService organizationService;

    private final OrganizationMemberService organizationMemberService;

    private final OrganizationRepository organizationRepository;

    public void execute(ESALRequestDto dto, EmailAddress memberEmail) {
        Organization organization = new Organization(dto.getName());
        ContactPerson contactPerson = organizationMemberService.fetch(memberEmail);
        organization.addUserAsMember(contactPerson);
        try {
            organizationRepository.save(organization);
        } catch (DataIntegrityViolationException ex) {
            throw new ESALAlreadyExists();
        }
    }

    public void execute(ESALRequestDto dto) {
        organizationService.create(dto);
    }
}
