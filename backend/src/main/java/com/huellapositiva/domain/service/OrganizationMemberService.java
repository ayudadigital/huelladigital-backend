package com.huellapositiva.domain.service;

import com.huellapositiva.application.exception.ConflictPersistingUserException;
import com.huellapositiva.domain.model.entities.ContactPerson;
import com.huellapositiva.domain.model.entities.User;
import com.huellapositiva.domain.model.valueobjects.*;
import com.huellapositiva.domain.repository.OrganizationMemberRepository;
import com.huellapositiva.infrastructure.orm.entities.Organization;
import com.huellapositiva.infrastructure.orm.entities.OrganizationMember;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class OrganizationMemberService {

    @Autowired
    private final OrganizationMemberRepository organizationMemberRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    public Integer registerMember(PlainPassword plainPassword, EmailConfirmation emailConfirmation) {
        try {
            PasswordHash hash = new PasswordHash(passwordEncoder.encode(plainPassword.toString()));
            ExpressRegistrationOrganizationMember expressOrganization = new ExpressRegistrationOrganizationMember(hash, emailConfirmation);
            return organizationMemberRepository.save(expressOrganization);
        } catch (DataIntegrityViolationException ex) {
            log.error("Unable to persist organization due to a conflict.", ex);
            throw new ConflictPersistingUserException("Conflict encountered while storing organization in database. Constraints were violated.", ex);
        }
    }

    public Integer updateJoinedOrganization(OrganizationMember employee, Organization organization) {
        return organizationMemberRepository.updateOrganization(employee.getId(), organization);
    }

    public Optional<OrganizationMember> findByEmail(String email){
        return organizationMemberRepository.findByEmail(email);
    }

    public ContactPerson fetch(EmailAddress emailAddress) {
        OrganizationMember organizationMember = organizationMemberRepository.findByEmail(emailAddress.toString()).get();
        if (organizationMember.getJoinedOrganization() == null) {
            return new ContactPerson(emailAddress, new Id(organizationMember.getId()));
        }
        com.huellapositiva.domain.model.entities.Organization esal = new com.huellapositiva.domain.model.entities.Organization(organizationMember.getJoinedOrganization().getName());
        return new ContactPerson(emailAddress, new Id(organizationMember.getId()), esal);
    }
}
