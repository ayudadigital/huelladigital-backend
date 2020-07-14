package com.huellapositiva.integration;

import com.huellapositiva.application.dto.CredentialsOrganizationEmployeeRequestDto;
import com.huellapositiva.domain.service.OrganizationEmployeeService;
import com.huellapositiva.domain.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.valueobjects.PlainPassword;
import com.huellapositiva.infrastructure.orm.repository.JpaOrganizationEmployeeRepository;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static com.huellapositiva.util.TestData.DEFAULT_EMAIL;
import static com.huellapositiva.util.TestData.DEFAULT_PASSWORD;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestData.class)
public class OrganizationServiceShould {
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
    void register_and_link_to_an_employee() {
        // GIVEN
        CredentialsOrganizationEmployeeRequestDto dto = CredentialsOrganizationEmployeeRequestDto.builder()
                .email(DEFAULT_EMAIL)
                .password(DEFAULT_PASSWORD)
                .build();
        Integer employeeId = organizationEmployeeService.registerEmployee(PlainPassword.from(dto.getPassword()), EmailConfirmation.from(dto.getEmail(), ""));

    }
}
