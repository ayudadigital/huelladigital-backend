package com.huellapositiva.domain.repository;

import com.huellapositiva.domain.ExpressRegistrationOrganizationEmployee;
import com.huellapositiva.infrastructure.orm.model.*;
import com.huellapositiva.infrastructure.orm.repository.JpaEmailConfirmationRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaOrganizationEmployeeRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaRoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static com.huellapositiva.domain.Roles.ORGANIZATION_EMPLOYEE_NOT_CONFIRMED;

@Component
@Transactional
@AllArgsConstructor
public class OrganizationEmployeeRepository {

    @Autowired
    private final JpaOrganizationEmployeeRepository jpaOrganizationEmployeeRepository;

    @Autowired
    private final JpaEmailConfirmationRepository jpaEmailConfirmationRepository;

    @Autowired
    private final JpaRoleRepository jpaRoleRepository;

    public OrganizationEmployee findById(Integer id) {
        return jpaOrganizationEmployeeRepository.findById(id)
                .orElseThrow( () -> new RuntimeException("Organization employee not found"));
    }

    public Integer save(ExpressRegistrationOrganizationEmployee expressEmployee) {
        Role role = jpaRoleRepository.findByName(ORGANIZATION_EMPLOYEE_NOT_CONFIRMED.toString())
                .orElseThrow(() -> new RuntimeException("Role ORGANIZATION_NOT_CONFIRMED not found."));
        EmailConfirmation emailConfirmation = EmailConfirmation.builder()
                .email(expressEmployee.getEmail())
                .hash(expressEmployee.getConfirmationToken())
                .build();
        emailConfirmation = jpaEmailConfirmationRepository.save(emailConfirmation);
        Credential credential = Credential.builder()
                .email(expressEmployee.getEmail())
                .hashedPassword(expressEmployee.getHashedPassword())
                .roles(Collections.singleton(role))
                .emailConfirmed(false)
                .emailConfirmation(emailConfirmation)
                .build();
        OrganizationEmployee organizationEmployee = OrganizationEmployee.builder()
                .credential(credential)
                .build();
        return jpaOrganizationEmployeeRepository.save(organizationEmployee).getId();
    }

    public Integer updateOrganization(Integer employeeId, Organization organization) {
        return jpaOrganizationEmployeeRepository.updateJoinedOrganization(employeeId, organization);
    }
}
