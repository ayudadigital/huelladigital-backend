package com.huellapositiva.domain.service;

import com.huellapositiva.application.dto.RegisterContactPersonDto;
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
     * @param plainPassword The contact person password
     * @param emailConfirmation Data on email confirmation
     * @return id of the contactPerson
     * @throws ConflictPersistingUserException when there's a problem with the data to be inserted in the DB
     */
    public Id registerContactPerson(RegisterContactPersonDto dto, PlainPassword plainPassword, EmailConfirmation emailConfirmation) {
        try {
            PasswordHash hash = new PasswordHash(passwordEncoder.encode(plainPassword.toString()));
            ContactPerson contactPerson = new ContactPerson(Id.newId(), EmailAddress.from(emailConfirmation.getEmailAddress()), hash, Id.newId(), dto);
            return esalContactPersonRepository.save(contactPerson, emailConfirmation);
        } catch (DataIntegrityViolationException ex) {
            log.error("Unable to persist organization due to a conflict.", ex);
            throw new ConflictPersistingUserException("Conflict encountered while storing organization in database. Constraints were violated.", ex);
        }
    }

    /**
     * This method updates the ESAL name linked with the contactPerson.
     *
     * @param jpaContactPerson jpaContactPerson to be updated
     * @param jpaESAL jpaESAL to be linked with the contactPerson
     * @return the number of rows updated in the DB
     */
    public Integer updateJoinedESAL(JpaContactPerson jpaContactPerson, JpaESAL jpaESAL) {
        return esalContactPersonRepository.updateESAL(jpaContactPerson.getId(), jpaESAL);
    }
}
