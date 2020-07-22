package com.huellapositiva.integration;

import com.huellapositiva.application.dto.CredentialsOrganizationMemberRequestDto;
import com.huellapositiva.domain.Roles;
import com.huellapositiva.domain.service.OrganizationMemberService;
import com.huellapositiva.domain.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.valueobjects.PlainPassword;
import com.huellapositiva.infrastructure.orm.model.Credential;
import com.huellapositiva.infrastructure.orm.model.OrganizationMember;
import com.huellapositiva.infrastructure.orm.repository.JpaOrganizationMemberRepository;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static com.huellapositiva.util.TestData.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestData.class)
class OrganizationMemberServiceShould {
    @Autowired
    private TestData testData;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private OrganizationMemberService organizationMemberService;

    @Autowired
    private JpaOrganizationMemberRepository organizationMemberRepository;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    void register_a_new_member() {
        // GIVEN
        CredentialsOrganizationMemberRequestDto dto = CredentialsOrganizationMemberRequestDto.builder()
                .email(DEFAULT_EMAIL)
                .password(DEFAULT_PASSWORD)
                .build();

        // WHEN
        Integer employeeId = organizationMemberService.registerMember(PlainPassword.from(dto.getPassword()), EmailConfirmation.from(dto.getEmail(), ""));

        // THEN
        Optional<OrganizationMember> employeeOptional = organizationMemberRepository.findByIdWithCredentialsAndRoles(employeeId);
        assertTrue(employeeOptional.isPresent());
        OrganizationMember organizationMember = employeeOptional.get();
        Credential credential = organizationMember.getCredential();
        assertThat(credential.getEmail(), is(DEFAULT_EMAIL));
        assertThat(passwordEncoder.matches(DEFAULT_PASSWORD, credential.getHashedPassword()), is(true));
        assertThat(credential.getRoles(), hasSize(1));
        assertThat(credential.getRoles().iterator().next().getName(), is(Roles.ORGANIZATION_MEMBER_NOT_CONFIRMED.toString()));
    }
}
