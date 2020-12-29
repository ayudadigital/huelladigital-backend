package com.huellapositiva.domain.repository;

import com.huellapositiva.domain.exception.RoleNotFoundException;
import com.huellapositiva.domain.model.entities.Volunteer;
import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.infrastructure.orm.entities.*;
import com.huellapositiva.infrastructure.orm.repository.JpaEmailConfirmationRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaProfileRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaRoleRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaVolunteerRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.NoSuchElementException;

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

    @Autowired
    private final JpaProfileRepository jpaProfileRepository;

    /**
     * This class managed the volunteer register. Add a role (VOLUNTEER_NOT_CONFIRMED), add row in DB the EmailConfirmation,
     * add the user credentials and add these points in Volunteer
     *
     * @param volunteer Send the CV, photo and user information
     * @param emailConfirmation Send the information about emailConfirmation
     */
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

    /**
     * This method return the volunteer full information stored in DB.
     *
     * @param email Email of volunteer to log
     */
    public Volunteer findByEmail(String email) {
        JpaVolunteer volunteer = jpaVolunteerRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("Could not find volunteer with email " + email)
        );
        return new Volunteer(
                EmailAddress.from(volunteer.getCredential().getEmail()),
                new Id(volunteer.getId()));
    }

    /**
     * This method store in DB the URL of Curriculum Vitae
     *
     * @param volunteer The volunteer information
     */
    public void updateCurriculumVitae(Volunteer volunteer) {
        JpaVolunteer jpaVolunteer = jpaVolunteerRepository.findById(volunteer.getId().toString())
                .orElseThrow(() -> new NoSuchElementException("No exists volunteer with: " + volunteer.getId()));
        jpaVolunteer.setCurriculumVitaeUrl(volunteer.getCurriculumVitae().toExternalForm());
        jpaVolunteerRepository.save(jpaVolunteer);
    }

    /**
     * This method store in DB the URL of user profile photo
     *
     * @param volunteer The volunteer information
     */
    public void updatePhoto(Volunteer volunteer) {
        JpaVolunteer jpaVolunteer = jpaVolunteerRepository.findById(volunteer.getId().toString())
                .orElseThrow(() -> new NoSuchElementException("No exists volunteer with: " + volunteer.getId()));

        boolean profileIsNull = jpaVolunteer.getProfile() == null;
        if (profileIsNull) {
            JpaProfile jpaProfile = JpaProfile.builder()
                    .id(Id.newId().toString())
                    .photoUrl(volunteer.getPhoto().toExternalForm())
                    .build();
            jpaProfileRepository.save(jpaProfile);
            jpaVolunteer.setProfile(jpaProfile);
        } else {
            jpaVolunteer.getProfile().setPhotoUrl(volunteer.getPhoto().toExternalForm());
        }
        jpaVolunteerRepository.save(jpaVolunteer);
    }

}
