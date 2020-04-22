package com.huellapositiva.domain.service;

import com.huellapositiva.domain.*;
import com.huellapositiva.domain.repository.VolunteerRepository;
import com.huellapositiva.domain.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.valueobjects.Password;
import com.huellapositiva.domain.valueobjects.PasswordHash;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class VolunteerService {

    @Autowired
    private final VolunteerRepository volunteerRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    public Integer registerVolunteer(Password password, EmailConfirmation emailConfirmation) {
        PasswordHash hash = new PasswordHash(passwordEncoder.encode(password.toString()));
        ExpressRegistrationVolunteer expressVolunteer = new ExpressRegistrationVolunteer(hash, emailConfirmation);
        return volunteerRepository.save(expressVolunteer);
    }
}
