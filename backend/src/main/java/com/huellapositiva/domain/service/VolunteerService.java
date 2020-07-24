package com.huellapositiva.domain.service;

import com.huellapositiva.application.exception.ConflictPersistingUserException;
import com.huellapositiva.domain.ExpressRegistrationVolunteer;
import com.huellapositiva.domain.repository.VolunteerRepository;
import com.huellapositiva.domain.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.valueobjects.PasswordHash;
import com.huellapositiva.domain.valueobjects.PlainPassword;
import com.huellapositiva.infrastructure.orm.model.Volunteer;
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

    public Integer registerVolunteer(PlainPassword plainPassword, EmailConfirmation emailConfirmation) {
        try {
            PasswordHash hash = new PasswordHash(passwordEncoder.encode(plainPassword.toString()));
            ExpressRegistrationVolunteer expressVolunteer = new ExpressRegistrationVolunteer(hash, emailConfirmation);
            return volunteerRepository.save(expressVolunteer);
        } catch (DataIntegrityViolationException ex) {
            log.error("Unable to persist volunteer due to a conflict.", ex);
            throw new ConflictPersistingUserException("Conflict encountered while storing user in database. Constraints were violated.", ex);
        }
    }

    public Volunteer findVolunteerByEmail(String email) {
        return volunteerRepository.findByEmail(email);
    }
}
