package com.huellapositiva.infrastructure.orm.service;

import com.huellapositiva.infrastructure.orm.entities.JpaCredential;
import com.huellapositiva.infrastructure.orm.repository.JpaCredentialRepository;
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
        JpaCredential jpaCredential = jpaCredentialRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));

        return new User(jpaCredential.getId(), jpaCredential.getHashedPassword(), getAuthority(jpaCredential));
    }

    private List<GrantedAuthority> getAuthority(JpaCredential jpaCredential) {
        return jpaCredential.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());
    }
}
