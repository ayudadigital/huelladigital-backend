package com.huellapositiva.domain.service;

import com.huellapositiva.application.dto.RegisterVolunteerRequestDto;
import com.huellapositiva.domain.*;
import com.huellapositiva.domain.repository.VolunteerRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class VolunteerService {

    @Autowired
    private final VolunteerRepository volunteerRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    public Integer registerVolunteer(RegisterVolunteerRequestDto dto) {
        Email email = Email.from(dto.getEmail());
        String hashedPassword = passwordEncoder.encode(dto.getPassword());
        Password password = Password.from(dto.getPassword(), hashedPassword);
        ExpressRegistrationVolunteer expressVolunteer = new ExpressRegistrationVolunteer(
                                                            email, password);
        return volunteerRepository.save(expressVolunteer);
    }
}
