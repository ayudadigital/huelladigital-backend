package com.huellapositiva.domain.service;

import com.huellapositiva.application.dto.RegisterVolunteerRequestDto;
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
    private final EmailConfirmationRepository emailConfirmationRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    public Integer registerVolunteer(RegisterVolunteerRequestDto dto) {
        Password password = Password.from(dto.getPassword());
        PasswordHash hash = new PasswordHash(passwordEncoder.encode(password.toString()));
        EmailConfirmation confirmation = EmailConfirmation.from(dto.getEmail());
        ExpressRegistrationVolunteer expressVolunteer = new ExpressRegistrationVolunteer(hash, confirmation);
        return volunteerRepository.save(expressVolunteer);
    }
}
