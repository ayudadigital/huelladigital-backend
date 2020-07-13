package com.huellapositiva.integration;

import com.huellapositiva.application.dto.CredentialsOrganizationEmployeeRequestDto;
import com.huellapositiva.domain.Roles;
import com.huellapositiva.domain.service.OrganizationEmployeeService;
import com.huellapositiva.domain.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.valueobjects.PlainPassword;
import com.huellapositiva.infrastructure.orm.model.Credential;
import com.huellapositiva.infrastructure.orm.model.OrganizationEmployee;
import com.huellapositiva.infrastructure.orm.repository.JpaOrganizationEmployeeRepository;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static com.huellapositiva.util.TestData.*;
import static com.huellapositiva.util.TestData.DEFAULT_ORGANIZATION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestData.class)
public class OrganizationEmployeeEmployeeServiceShould {
    @Autowired
    private TestData testData;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private OrganizationEmployeeService organizationEmployeeService;

    @Autowired
    private JpaOrganizationEmployeeRepository organizationEmployeeRepository;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    void register_a_new_employee() {
        // GIVEN
        CredentialsOrganizationEmployeeRequestDto dto = CredentialsOrganizationEmployeeRequestDto.builder()
                .email(DEFAULT_EMAIL)
                .password(DEFAULT_PASSWORD)
                .build();

        // WHEN
        Integer employeeId = organizationEmployeeService.registerEmployee(PlainPassword.from(dto.getPassword()), EmailConfirmation.from(dto.getEmail(), ""));

        // THEN
        Optional<OrganizationEmployee> employeeOptional = organizationEmployeeRepository.findByIdWithCredentialsAndRoles(employeeId);
        assertTrue(employeeOptional.isPresent());
        OrganizationEmployee organizationEmployee = employeeOptional.get();
        Credential credential = organizationEmployee.getCredential();
        assertThat(credential.getEmail(), is(DEFAULT_EMAIL));
        assertThat(passwordEncoder.matches(DEFAULT_PASSWORD, credential.getHashedPassword()), is(true));
        assertThat(credential.getRoles(), hasSize(1));
        assertThat(credential.getRoles().iterator().next().getName(), is(Roles.ORGANIZATION_EMPLOYEE_NOT_CONFIRMED.toString()));
    }
}
