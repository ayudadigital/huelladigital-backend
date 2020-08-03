package com.huellapositiva.domain.service;

import com.huellapositiva.application.exception.FailedToPersistUser;
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

    public User fetch(EmailAddress emailAddress) {
        OrganizationMember organizationMember = organizationMemberRepository.findByEmail(emailAddress.toString()).get();
        return new User(emailAddress, new Id(organizationMember.getId()));
    }
}
