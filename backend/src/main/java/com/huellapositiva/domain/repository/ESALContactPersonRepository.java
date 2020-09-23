package com.huellapositiva.domain.repository;

import com.huellapositiva.application.exception.UserNotFound;
import com.huellapositiva.domain.model.entities.ContactPerson;
import com.huellapositiva.domain.model.entities.ESAL;
import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.infrastructure.orm.entities.*;
import com.huellapositiva.infrastructure.orm.repository.JpaContactPersonRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaEmailConfirmationRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaRoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

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
    private final JpaRoleRepository jpaRoleRepository;

    public JpaContactPerson findById(Integer id) {
        return jpaContactPersonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization member not found"));
    }

    public Id save(ContactPerson contactPerson, com.huellapositiva.domain.model.valueobjects.EmailConfirmation emailConfirmation) {
        Role role = jpaRoleRepository.findByName(CONTACT_PERSON_NOT_CONFIRMED.toString())
                .orElseThrow(() -> new RuntimeException("Role CONTACT_PERSON_NOT_CONFIRMED not found."));
        EmailConfirmation jpaEmailConfirmation = EmailConfirmation.builder()
                .email(contactPerson.getEmailAddress().toString())
                .hash(emailConfirmation.getToken())
                .build();
        jpaEmailConfirmation = jpaEmailConfirmationRepository.save(jpaEmailConfirmation);
        JpaCredential jpaCredential = JpaCredential.builder()
                .email(contactPerson.getEmailAddress().toString())
                .hashedPassword(contactPerson.getPasswordHash().toString())
                .roles(Collections.singleton(role))
                .emailConfirmed(false)
                .emailConfirmation(jpaEmailConfirmation)
                .build();
        JpaContactPerson jpaContactPerson = JpaContactPerson.builder()
                .credential(jpaCredential)
                .id(contactPerson.getId().getValue())
                .build();
        jpaContactPersonRepository.save(jpaContactPerson);
        return contactPerson.getId();
    }

    public Integer updateESAL(String employeeId, JpaESAL organization) {
        return jpaContactPersonRepository.updateJoinedESAL(employeeId, organization);
    }

    public Optional<JpaContactPerson> findByEmail(String email) {
        return jpaContactPersonRepository.findByEmail(email);
    }

    public ESAL getJoinedESAL(String contactPersonEmail) {
        JpaContactPerson jpaContactPerson = jpaContactPersonRepository.findByEmail(contactPersonEmail)
                .orElseThrow(UserNotFound::new);
        return new ESAL(jpaContactPerson.getJoinedEsal().getName(),
                new Id(jpaContactPerson.getJoinedEsal().getId()),
                EmailAddress.from(contactPersonEmail));
    }
}
