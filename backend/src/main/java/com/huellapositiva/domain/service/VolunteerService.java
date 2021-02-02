package com.huellapositiva.domain.service;

import com.huellapositiva.application.exception.ConflictPersistingUserException;
import com.huellapositiva.domain.model.entities.Volunteer;
import com.huellapositiva.domain.model.valueobjects.*;
import com.huellapositiva.domain.repository.VolunteerRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class VolunteerService {

    @Autowired
    private final VolunteerRepository volunteerRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    public Volunteer registerVolunteer(PlainPassword plainPassword, EmailConfirmation emailConfirmation) {
        try {
            PasswordHash passwordHash = new PasswordHash(passwordEncoder.encode(plainPassword.toString()));
            Volunteer volunteer = new Volunteer(Id.newId(), EmailAddress.from(emailConfirmation.getEmailAddress()), passwordHash, Id.newId());
            return volunteerRepository.save(volunteer, emailConfirmation);
        } catch (DataIntegrityViolationException ex) {
            log.error("Unable to persist volunteer due to a conflict.", ex);
            throw new ConflictPersistingUserException("Conflict encountered while storing user in database. Constraints were violated.", ex);
        }
    }

}
