package com.huellapositiva.domain.service;

import com.huellapositiva.application.exception.FailedToPersistUser;
import com.huellapositiva.domain.ExpressRegistrationOrganization;
import com.huellapositiva.domain.repository.OrganizationRepository;
import com.huellapositiva.domain.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.valueobjects.PasswordHash;
import com.huellapositiva.domain.valueobjects.PlainPassword;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class OrganizationService {

    @Autowired
    private final OrganizationRepository organizationRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    public Integer registerOrganization(PlainPassword plainPassword, EmailConfirmation emailConfirmation, String name) {
        try {
            PasswordHash hash = new PasswordHash(passwordEncoder.encode(plainPassword.toString()));
            ExpressRegistrationOrganization expressOrganization = new ExpressRegistrationOrganization(hash, emailConfirmation, name);
            return organizationRepository.save(expressOrganization);
        } catch (DataIntegrityViolationException ex) {
            log.error("Unable to persist organization due to a conflict.", ex);
            throw new FailedToPersistUser("Conflict encountered while storing organization in database. Constraints were violated.", ex);
        }
    }
}
