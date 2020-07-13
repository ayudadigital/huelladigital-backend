package com.huellapositiva.domain.repository;

import com.huellapositiva.domain.ExpressRegistrationOrganization;
import com.huellapositiva.infrastructure.orm.model.Organization;
import com.huellapositiva.infrastructure.orm.repository.JpaOrganizationEmployeeRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaOrganizationRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Component
@Transactional
@AllArgsConstructor
public class OrganizationRepository {

    @Autowired
    private final JpaOrganizationRepository jpaOrganizationRepository;

    @Autowired
    private final JpaOrganizationEmployeeRepository jpaOrganizationEmployeeRepository;

    public Integer save(ExpressRegistrationOrganization expressOrganization) {
        Organization organization = Organization.builder()
                .name(expressOrganization.getName())
                .employees(Collections.singletonList(expressOrganization.getEmployee()))
                .build();
        return jpaOrganizationRepository.save(organization).getId();
    }

    public Organization findById(Integer id) {
        return jpaOrganizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Could not find the organization by the provided ID"));
    }
}
