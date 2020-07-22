package com.huellapositiva.domain.service;

import com.huellapositiva.application.exception.FailedToPersistUser;
import com.huellapositiva.domain.ExpressRegistrationOrganizationMember;
import com.huellapositiva.domain.repository.OrganizationMemberRepository;
import com.huellapositiva.domain.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.valueobjects.PasswordHash;
import com.huellapositiva.domain.valueobjects.PlainPassword;
import com.huellapositiva.infrastructure.orm.model.Organization;
import com.huellapositiva.infrastructure.orm.model.OrganizationMember;
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
            throw new FailedToPersistUser("Conflict encountered while storing organization in database. Constraints were violated.", ex);
        }
    }

    public Integer updateJoinedOrganization(OrganizationMember employee, Organization organization) {
        return organizationMemberRepository.updateOrganization(employee.getId(), organization);
    }

    public Optional<OrganizationMember> findByEmail(String email){
        return organizationMemberRepository.findByEmail(email);
    }
}
