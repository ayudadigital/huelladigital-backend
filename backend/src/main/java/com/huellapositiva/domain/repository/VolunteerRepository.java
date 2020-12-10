package com.huellapositiva.domain.repository;

import com.huellapositiva.domain.exception.RoleNotFoundException;
import com.huellapositiva.domain.model.entities.Volunteer;
import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.infrastructure.orm.entities.EmailConfirmation;
import com.huellapositiva.infrastructure.orm.entities.JpaCredential;
import com.huellapositiva.infrastructure.orm.entities.JpaVolunteer;
import com.huellapositiva.infrastructure.orm.entities.Role;
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

    public Volunteer save(Volunteer volunteer, com.huellapositiva.domain.model.valueobjects.EmailConfirmation emailConfirmation) {
        Role role = jpaRoleRepository.findByName(VOLUNTEER_NOT_CONFIRMED.toString())
                .orElseThrow(() -> new RoleNotFoundException("Role VOLUNTEER_NOT_CONFIRMED not found."));
        EmailConfirmation jpaEmailConfirmation = EmailConfirmation.builder()
                .email(volunteer.getEmailAddress().toString())
                .hash(emailConfirmation.getToken())
                .build();
        jpaEmailConfirmation = jpaEmailConfirmationRepository.save(jpaEmailConfirmation);
        JpaCredential jpaCredential = JpaCredential.builder()
                .email(volunteer.getEmailAddress().toString())
                .hashedPassword(volunteer.getPasswordHash().toString())
                .roles(Collections.singleton(role))
                .emailConfirmed(false)
                .emailConfirmation(jpaEmailConfirmation)
                .build();
        JpaVolunteer jpaVolunteer = JpaVolunteer.builder()
                .id(volunteer.getId().getValue())
                .credential(jpaCredential)
                .build();
        jpaVolunteerRepository.save(jpaVolunteer);
        return volunteer;
    }

    public Volunteer findByEmail(String email) {
        JpaVolunteer volunteer = jpaVolunteerRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("Could not find volunteer with email " + email)
        );
        return new Volunteer(
                EmailAddress.from(volunteer.getCredential().getEmail()),
                new Id(volunteer.getId()));
    }

    public void updateCurriculumVitae(Volunteer volunteer) {
        jpaVolunteerRepository.updateCurriculumVitae(volunteer.getId().toString(), volunteer.getCurriculumVitae().toExternalForm());
    }

    public void updatePhoto(Volunteer volunteer) {
        jpaVolunteerRepository.updatePhoto(volunteer.getId().toString(), volunteer.getPhoto().toExternalForm());
    }
}
