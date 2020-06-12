package com.huellapositiva.domain.repository;

import com.huellapositiva.domain.*;
import com.huellapositiva.domain.exception.RoleNotFoundException;
import com.huellapositiva.infrastructure.orm.model.*;
import com.huellapositiva.infrastructure.orm.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Set;

@Component
@Transactional
@AllArgsConstructor
public class VolunteerRepository {

    @Autowired
    private final JpaVolunteerRepository jpaVolunteerRepository;

    @Autowired
    private final JpaEmailConfirmationRepository jpaEmailConfirmationRepository;

    @Autowired
    private final JpaRoleRepository jpaRoleRepository;

    @Autowired
    private final JpaFailEmailConfirmationRepository jpaFailEmailConfirmationRepository;

    @Autowired
    private final JpaCredentialRepository jpaCredentialRepository;

    public Integer save(ExpressRegistrationVolunteer expressVolunteer) {
        Role role = jpaRoleRepository.findByName(Roles.VOLUNTEER_NOT_CONFIRMED.toString())
                .orElseThrow(() -> new RoleNotFoundException("Role VOLUNTEER not found."));
        EmailConfirmation emailConfirmation = EmailConfirmation.builder()
                .email(expressVolunteer.getEmail())
                .hash(expressVolunteer.getConfirmationToken())
                .build();
        emailConfirmation = jpaEmailConfirmationRepository.save(emailConfirmation);
        Credential credential = Credential.builder()
                .email(expressVolunteer.getEmail())
                .hashedPassword(expressVolunteer.getHashedPassword())
                .roles(Collections.singleton(role))
                .emailConfirmed(false)
                .emailConfirmation(emailConfirmation)
                .build();
        Volunteer volunteer = Volunteer.builder()
                .credential(credential)
                .build();
        return jpaVolunteerRepository.save(volunteer).getId();
    }

    public Set<Role> getRoles(String email) {
        return jpaCredentialRepository.findByEmail(email).orElse(null).getRoles();
    }


}
