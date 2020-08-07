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

    public Id registerMember(PlainPassword plainPassword, EmailConfirmation emailConfirmation) {
        try {
            PasswordHash hash = new PasswordHash(passwordEncoder.encode(plainPassword.toString()));
            ExpressRegistrationESALMember expressOrganization = new ExpressRegistrationESALMember(hash, emailConfirmation);
            return esalContactPersonRepository.save(expressOrganization);
        } catch (DataIntegrityViolationException ex) {
            log.error("Unable to persist organization due to a conflict.", ex);
            throw new ConflictPersistingUserException("Conflict encountered while storing organization in database. Constraints were violated.", ex);
        }
    }

    public Integer updateJoinedOrganization(JpaContactPerson employee, JpaESAL organization) {
        return esalContactPersonRepository.updateOrganization(employee.getId(), organization);
    }

    public Optional<JpaContactPerson> findByEmail(String email){
        return esalContactPersonRepository.findByEmail(email);
    }

//    public com.huellapositiva.domain.model.entities.ContactPerson fetch(EmailAddress emailAddress) {
//        JpaContactPerson contactPerson = esalContactPersonRepository.findByEmail(emailAddress.toString()).get();
//        if (contactPerson.getJoinedEsal() == null) {
//            return com.huellapositiva.domain.model.entities.ContactPerson.contactPersonWithoutESAL(emailAddress, new Id(contactPerson.getId()));
//        }
//        com.huellapositiva.domain.model.entities.ESAL esal = new com.huellapositiva.domain.model.entities.ESAL(contactPerson.getJoinedEsal().getName());
//        return com.huellapositiva.domain.model.entities.ContactPerson.contactPersonWithESAL(emailAddress, new Id(contactPerson.getId()), esal);
//    }
}
