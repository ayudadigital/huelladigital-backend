package com.huellapositiva.domain.service;

import com.huellapositiva.application.dto.RegisterVolunteerRequestDto;
import com.huellapositiva.domain.Credential;
import com.huellapositiva.domain.Role;
import com.huellapositiva.domain.Roles;
import com.huellapositiva.domain.Volunteer;
import com.huellapositiva.domain.exception.RoleNotFound;
import com.huellapositiva.domain.repository.RoleRepository;
import com.huellapositiva.domain.repository.VolunteerRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@Transactional
@AllArgsConstructor
public class VolunteerService {

    @Autowired
    private final RoleRepository roleRepository;

    @Autowired
    private final VolunteerRepository volunteerRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    public Integer registerVolunteer(RegisterVolunteerRequestDto dto) {
        Role role = roleRepository.findByName(Roles.VOLUNTEER.toString())
                .orElseThrow(() -> new RoleNotFound("Role VOLUNTEER not found."));
        String hashedPassword = passwordEncoder.encode(dto.getPassword());
        Credential credential = Credential.builder()
                .email(dto.getEmail())
                .hashedPassword(hashedPassword)
                .roles(Collections.singleton(role))
                .build();
        Volunteer volunteer = Volunteer.builder()
                .credential(credential)
                .build();
        volunteer = volunteerRepository.save(volunteer);
        return volunteer.getId();
    }
}
