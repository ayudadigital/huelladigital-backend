package com.huellapositiva.integration;

import com.huellapositiva.application.dto.AuthenticationRequestDto;
import com.huellapositiva.domain.model.entities.Volunteer;
import com.huellapositiva.domain.model.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.model.valueobjects.PlainPassword;
import com.huellapositiva.domain.model.valueobjects.Roles;
import com.huellapositiva.domain.service.VolunteerService;
import com.huellapositiva.infrastructure.orm.entities.JpaCredential;
import com.huellapositiva.infrastructure.orm.entities.JpaVolunteer;
import com.huellapositiva.infrastructure.orm.repository.JpaVolunteerRepository;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static com.huellapositiva.util.TestData.DEFAULT_EMAIL;
import static com.huellapositiva.util.TestData.DEFAULT_PASSWORD;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestData.class)
class VolunteerServiceShould {

    @Autowired
    private TestData testData;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private VolunteerService volunteerService;

    @Autowired
    private JpaVolunteerRepository volunteerRepository;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    void registering_a_volunteer_should_create_the_corresponding_entities_in_the_db() {
        AuthenticationRequestDto dto = AuthenticationRequestDto.builder()
                .email(DEFAULT_EMAIL)
                .password(DEFAULT_PASSWORD)
                .build();

        Volunteer volunteerEntity = volunteerService.registerVolunteer(PlainPassword.from(dto.getPassword()), EmailConfirmation.from(dto.getEmail(), ""));

        Optional<JpaVolunteer> volunteerOptional = volunteerRepository.findByIdWithCredentialsAndRoles(volunteerEntity.getId().toString());
        assertTrue(volunteerOptional.isPresent());
        JpaVolunteer volunteer = volunteerOptional.get();
        JpaCredential jpaCredential = volunteer.getCredential();
        assertThat(jpaCredential.getEmail(), is(DEFAULT_EMAIL));
        assertThat(passwordEncoder.matches(DEFAULT_PASSWORD, jpaCredential.getHashedPassword()), is(true));
        assertThat(jpaCredential.getRoles(), hasSize(1));
        assertThat(jpaCredential.getRoles().iterator().next().getName(), is(Roles.VOLUNTEER_NOT_CONFIRMED.toString()));
    }
}
