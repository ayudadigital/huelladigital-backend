package com.huellapositiva.integration;

import com.huellapositiva.application.dto.RegisterContactPersonDto;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.domain.model.valueobjects.Roles;
import com.huellapositiva.domain.service.ESALContactPersonService;
import com.huellapositiva.domain.model.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.model.valueobjects.PlainPassword;
import com.huellapositiva.infrastructure.orm.entities.JpaCredential;
import com.huellapositiva.infrastructure.orm.entities.JpaContactPerson;
import com.huellapositiva.infrastructure.orm.repository.JpaContactPersonProfileRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaContactPersonRepository;
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
class ESALContactPersonServiceShould {
    @Autowired
    private TestData testData;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private ESALContactPersonService ESALContactPersonService;

    @Autowired
    private JpaContactPersonRepository organizationMemberRepository;

    @Autowired
    private JpaContactPersonProfileRepository organizationMemberProfileRepository;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    void register_a_new_member() {
        // GIVEN
        RegisterContactPersonDto dto = RegisterContactPersonDto.builder()
                .name(VALID_NAME)
                .surname(VALID_SURNAME)
                .phoneNumber(VALID_PHONE)
                .email(DEFAULT_EMAIL)
                .password(DEFAULT_PASSWORD)
                .build();

        // WHEN
        Id contactPersonId = ESALContactPersonService.registerContactPerson(dto, PlainPassword.from(dto.getPassword()), EmailConfirmation.from(dto.getEmail(), ""));

        // THEN
        Optional<JpaContactPerson> employeeOptional = organizationMemberRepository.findByIdWithCredentialsAndRoles(contactPersonId.toString());
        assertTrue(employeeOptional.isPresent());
        JpaContactPerson contactPerson = employeeOptional.get();
        JpaCredential jpaCredential = contactPerson.getCredential();
        assertThat(jpaCredential.getEmail(), is(DEFAULT_EMAIL));
        assertThat(passwordEncoder.matches(DEFAULT_PASSWORD, jpaCredential.getHashedPassword()), is(true));
        assertThat(jpaCredential.getRoles(), hasSize(1));
        assertThat(jpaCredential.getRoles().iterator().next().getName(), is(Roles.CONTACT_PERSON_NOT_CONFIRMED.toString()));
    }
}
