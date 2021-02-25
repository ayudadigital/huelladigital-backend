package com.huellapositiva.domain.repository;

import com.huellapositiva.application.exception.UserNotFoundException;
import com.huellapositiva.domain.model.entities.ContactPerson;
import com.huellapositiva.domain.model.entities.ESAL;
import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.domain.model.valueobjects.PasswordHash;
import com.huellapositiva.infrastructure.orm.entities.*;
import com.huellapositiva.infrastructure.orm.repository.JpaContactPersonProfileRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaContactPersonRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaEmailConfirmationRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaRoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static com.huellapositiva.domain.model.valueobjects.Roles.CONTACT_PERSON_NOT_CONFIRMED;

@Component
@Transactional
@AllArgsConstructor
public class ESALContactPersonRepository {

    @Autowired
    private final JpaContactPersonRepository jpaContactPersonRepository;

    @Autowired
    private final JpaEmailConfirmationRepository jpaEmailConfirmationRepository;

    @Autowired
    private final JpaContactPersonProfileRepository jpaContactPersonProfileRepository;

    @Autowired
    private final JpaRoleRepository jpaRoleRepository;

    public JpaContactPerson findById(Integer id) {
        return jpaContactPersonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization member not found"));
    }

    public Id save(ContactPerson contactPerson, com.huellapositiva.domain.model.valueobjects.EmailConfirmation emailConfirmation) {
        Role role = jpaRoleRepository.findByName(CONTACT_PERSON_NOT_CONFIRMED.toString())
                .orElseThrow(() -> new RuntimeException("Role CONTACT_PERSON_NOT_CONFIRMED not found."));
        JpaEmailConfirmation jpaEmailConfirmation = JpaEmailConfirmation.builder()
                .email(contactPerson.getEmailAddress().toString())
                .hash(emailConfirmation.getToken())
                .build();
        jpaEmailConfirmation = jpaEmailConfirmationRepository.save(jpaEmailConfirmation);
        JpaCredential jpaCredential = JpaCredential.builder()
                .id(contactPerson.getAccountId().getValue())
                .email(contactPerson.getEmailAddress().toString())
                .hashedPassword(contactPerson.getPasswordHash().toString())
                .roles(Collections.singleton(role))
                .emailConfirmed(false)
                .emailConfirmation(jpaEmailConfirmation)
                .build();
        JpaContactPersonProfile jpaContactPersonProfile = JpaContactPersonProfile.builder()
                .id(Id.newId().getValue())
                .name(contactPerson.getName())
                .surname(contactPerson.getSurname())
                .phoneNumber(contactPerson.getPhoneNumber())
                .build();
        jpaContactPersonProfileRepository.save(jpaContactPersonProfile);
        JpaContactPerson jpaContactPerson = JpaContactPerson.builder()
                .credential(jpaCredential)
                .id(contactPerson.getId().getValue())
                .contactPersonProfile(jpaContactPersonProfile)
                .build();
        jpaContactPersonRepository.save(jpaContactPerson);
        return contactPerson.getId();
    }

    public Integer updateESAL(String employeeId, JpaESAL organization) {
        return jpaContactPersonRepository.updateJoinedESAL(employeeId, organization);
    }

    public ContactPerson findByAccountId(String accountId) {
        JpaContactPerson jpaContactPerson = jpaContactPersonRepository.findByAccountId(accountId)
                .orElseThrow(() -> new UserNotFoundException("Could not find contact person with account ID: " + accountId));

        ContactPerson contactPerson = new ContactPerson(
                new Id(jpaContactPerson.getCredential().getId()),
                EmailAddress.from(jpaContactPerson.getCredential().getEmail()),
                new PasswordHash(jpaContactPerson.getCredential().getHashedPassword()),
                new Id(jpaContactPerson.getId()));

        JpaESAL jpaESAL = jpaContactPerson.getJoinedEsal();
        if (jpaESAL != null) {
            ESAL esal = ESAL.fromJpa(jpaESAL);
            contactPerson.setJoinedEsal(esal);
        }

        return contactPerson;
    }

    public ESAL getJoinedESAL(String accountId) {
        JpaContactPerson jpaContactPerson = jpaContactPersonRepository.findByAccountId(accountId)
                .orElseThrow(() -> new UserNotFoundException("User not found. Account ID: " + accountId));
        ESAL esal = ESAL.fromJpa(jpaContactPerson.getJoinedEsal());
        esal.setContactPersonEmail(EmailAddress.from(jpaContactPerson.getCredential().getEmail()));
        return esal;
    }
}
