package com.huellapositiva.domain.service;

import com.huellapositiva.application.exception.FailedToPersistUser;
import com.huellapositiva.domain.ExpressRegistrationOrganizationEmployee;
import com.huellapositiva.domain.repository.OrganizationEmployeeRepository;
import com.huellapositiva.domain.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.valueobjects.PasswordHash;
import com.huellapositiva.domain.valueobjects.PlainPassword;
import com.huellapositiva.infrastructure.orm.model.Organization;
import com.huellapositiva.infrastructure.orm.model.OrganizationEmployee;
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
public class OrganizationEmployeeService {

    @Autowired
    private final OrganizationEmployeeRepository organizationEmployeeRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    public Integer registerEmployee(PlainPassword plainPassword, EmailConfirmation emailConfirmation) {
        try {
            PasswordHash hash = new PasswordHash(passwordEncoder.encode(plainPassword.toString()));
            ExpressRegistrationOrganizationEmployee expressOrganization = new ExpressRegistrationOrganizationEmployee(hash, emailConfirmation);
            return organizationEmployeeRepository.save(expressOrganization);
        } catch (DataIntegrityViolationException ex) {
            log.error("Unable to persist organization due to a conflict.", ex);
            throw new FailedToPersistUser("Conflict encountered while storing organization in database. Constraints were violated.", ex);
        }
    }

    public Integer updateJoinedOrganization(OrganizationEmployee employee, Organization organization) {
        return organizationEmployeeRepository.updateOrganization(employee.getId(), organization);
    }

    public Optional<OrganizationEmployee> findByEmail(String email){
        return organizationEmployeeRepository.findByEmail(email);
    }
}
