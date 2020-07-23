package com.huellapositiva.domain.repository;

import com.huellapositiva.domain.model.valueobjects.ExpressRegistrationVolunteer;
import com.huellapositiva.domain.exception.RoleNotFoundException;
import com.huellapositiva.infrastructure.orm.entities.Credential;
import com.huellapositiva.infrastructure.orm.entities.EmailConfirmation;
import com.huellapositiva.infrastructure.orm.entities.Role;
import com.huellapositiva.infrastructure.orm.entities.Volunteer;
import com.huellapositiva.infrastructure.orm.repository.JpaEmailConfirmationRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaRoleRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaVolunteerRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static com.huellapositiva.domain.model.valueobjects.Roles.VOLUNTEER_NOT_CONFIRMED;

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

    public Integer save(ExpressRegistrationVolunteer expressVolunteer) {
        Role role = jpaRoleRepository.findByName(VOLUNTEER_NOT_CONFIRMED.toString())
                .orElseThrow(() -> new RoleNotFoundException("Role VOLUNTEER_NOT_CONFIRMED not found."));
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

    public Volunteer findByEmail(String email) {
        return jpaVolunteerRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("Could not find volunteer with email " + email)
        );
    }
}
