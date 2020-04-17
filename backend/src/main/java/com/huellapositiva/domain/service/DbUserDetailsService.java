package com.huellapositiva.domain.service;

import com.huellapositiva.domain.Credential;
import com.huellapositiva.infrastructure.orm.JpaCredentialRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
@Transactional
public class DbUserDetailsService implements UserDetailsService {

    private final JpaCredentialRepository jpaCredentialRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        Credential credential = jpaCredentialRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Patient with username: " + email + " was not found."));

        return new User(credential.getEmail(), credential.getHashedPassword(), getAuthority(credential));
    }

    private List<GrantedAuthority> getAuthority(Credential credential) {
        return credential.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());
    }
}
