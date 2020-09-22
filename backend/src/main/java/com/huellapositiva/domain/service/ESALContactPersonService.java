package com.huellapositiva.domain.service;

import com.huellapositiva.application.exception.ConflictPersistingUserException;
import com.huellapositiva.domain.model.valueobjects.*;
import com.huellapositiva.domain.repository.ESALContactPersonRepository;
import com.huellapositiva.infrastructure.orm.entities.JpaESAL;
import com.huellapositiva.infrastructure.orm.entities.JpaContactPerson;
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
public class ESALContactPersonService {

    @Autowired
    private final ESALContactPersonRepository esalContactPersonRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    /**
     * This method registers a contactPerson in the DB
     *
     * @param plainPassword
     * @param emailConfirmation
     * @return id of the contactPerson
     * @throws ConflictPersistingUserException when there's a problem with the data to be inserted in the DB
     */
    public Id registerContactPerson(PlainPassword plainPassword, EmailConfirmation emailConfirmation) {
        try {
            PasswordHash hash = new PasswordHash(passwordEncoder.encode(plainPassword.toString()));
            ExpressRegistrationESALMember expressOrganization = new ExpressRegistrationESALMember(hash, emailConfirmation);
            return esalContactPersonRepository.save(expressOrganization);
        } catch (DataIntegrityViolationException ex) {
            log.error("Unable to persist organization due to a conflict.", ex);
            throw new ConflictPersistingUserException("Conflict encountered while storing organization in database. Constraints were violated.", ex);
        }
    }

    /**
     *
     * TO BE DEFINED
     */
    public Integer updateJoinedESAL(JpaContactPerson jpaContactPerson, JpaESAL jpaESAL) {
        return esalContactPersonRepository.updateESAL(jpaContactPerson.getId(), jpaESAL);
    }

}
