package com.huellapositiva.domain.service;

import com.huellapositiva.domain.ExpressRegistrationVolunteer;
import com.huellapositiva.domain.repository.VolunteerRepository;
import com.huellapositiva.domain.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.valueobjects.PasswordHash;
import com.huellapositiva.domain.valueobjects.PlainPassword;
import com.huellapositiva.infrastructure.orm.model.Role;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class VolunteerService {

    @Autowired
    private final VolunteerRepository volunteerRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    public Integer registerVolunteer(PlainPassword plainPassword, EmailConfirmation emailConfirmation) {
        PasswordHash hash = new PasswordHash(passwordEncoder.encode(plainPassword.toString()));
        ExpressRegistrationVolunteer expressVolunteer = new ExpressRegistrationVolunteer(hash, emailConfirmation);
        return volunteerRepository.save(expressVolunteer);
    }

    public List<String> getVolunteerRoles(String email) {
        return volunteerRepository.getRoles(email).stream().map(Role::getName).collect(Collectors.toList());
    }
}
