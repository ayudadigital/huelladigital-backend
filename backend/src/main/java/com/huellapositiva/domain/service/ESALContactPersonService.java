package com.huellapositiva.domain.service;

import com.huellapositiva.application.exception.ConflictPersistingUserException;
import com.huellapositiva.domain.model.entities.ContactPerson;
import com.huellapositiva.domain.model.valueobjects.*;
import com.huellapositiva.domain.repository.ESALContactPersonRepository;
import com.huellapositiva.infrastructure.orm.entities.JpaContactPerson;
import com.huellapositiva.infrastructure.orm.entities.JpaESAL;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
            ContactPerson contactPerson = new ContactPerson(EmailAddress.from(emailConfirmation.getEmailAddress()), hash, Id.newId());
            return esalContactPersonRepository.save(contactPerson, emailConfirmation);
        } catch (DataIntegrityViolationException ex) {
            log.error("Unable to persist organization due to a conflict.", ex);
            throw new ConflictPersistingUserException("Conflict encountered while storing organization in database. Constraints were violated.", ex);
        }
    }

    /**
     * This method updates the ESAL name linked with the contactPerson.
     *
     * @param jpaContactPerson Contactperson to be updated
     * @param jpaESAL JpaESAL to be linked with the contactPerson
     * @return the number of rows updated in the DB
     */
    public Integer updateJoinedESAL(JpaContactPerson jpaContactPerson, JpaESAL jpaESAL) {
        return esalContactPersonRepository.updateESAL(jpaContactPerson.getId(), jpaESAL);
    }

}
