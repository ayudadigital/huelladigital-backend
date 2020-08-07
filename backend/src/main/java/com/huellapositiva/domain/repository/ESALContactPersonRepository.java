package com.huellapositiva.domain.repository;

import com.huellapositiva.application.exception.UserNotFound;
import com.huellapositiva.domain.model.entities.ESAL;
import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.valueobjects.ExpressRegistrationESALMember;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.infrastructure.orm.entities.*;
import com.huellapositiva.infrastructure.orm.repository.JpaEmailConfirmationRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaContactPersonRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaRoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

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

    public Id save(ExpressRegistrationESALMember expressMember) {
        Role role = jpaRoleRepository.findByName(CONTACT_PERSON_NOT_CONFIRMED.toString())
                .orElseThrow(() -> new RuntimeException("Role ORGANIZATION_MEMBER_NOT_CONFIRMED not found."));
        EmailConfirmation emailConfirmation = EmailConfirmation.builder()
                .email(expressMember.getEmail())
                .hash(expressMember.getConfirmationToken())
                .build();
        emailConfirmation = jpaEmailConfirmationRepository.save(emailConfirmation);
        Credential credential = Credential.builder()
                .email(expressMember.getEmail())
                .hashedPassword(expressMember.getHashedPassword())
                .roles(Collections.singleton(role))
                .emailConfirmed(false)
                .emailConfirmation(emailConfirmation)
                .build();
        JpaContactPerson contactPerson = JpaContactPerson.builder()
                .credential(credential)
                .id(UUID.randomUUID().toString())
                .build();
        contactPerson = jpaContactPersonRepository.save(contactPerson);
        return new Id(contactPerson.getId());
    }

    public Integer updateOrganization(String employeeId, JpaESAL organization) {
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
