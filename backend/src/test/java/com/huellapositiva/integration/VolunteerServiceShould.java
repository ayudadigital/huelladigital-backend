package com.huellapositiva.integration;

import com.huellapositiva.application.dto.RegisterVolunteerRequestDto;
import com.huellapositiva.domain.Credential;
import com.huellapositiva.domain.Roles;
import com.huellapositiva.domain.Volunteer;
import com.huellapositiva.infrastructure.orm.JpaVolunteerRepository;
import com.huellapositiva.domain.service.VolunteerService;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestData.class)
class VolunteerServiceShould {

    @Autowired
    private MockMvc mvc;

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
        String email = "foo@huellapositiva.com";
        String password = "password";
        RegisterVolunteerRequestDto dto = RegisterVolunteerRequestDto.builder()
                .email(email)
                .password(password)
                .build();

        Integer volunteerId = volunteerService.registerVolunteer(dto);

        Volunteer volunteer = volunteerRepository.findByIdWithCredentialsAndRoles(volunteerId).get();
        Credential credential = volunteer.getCredential();
        assertThat(credential.getEmail(), is(email));
        assertThat(passwordEncoder.matches(password, credential.getHashedPassword()), is(true));
        assertThat(credential.getRoles(), hasSize(1));
        assertThat(credential.getRoles().iterator().next().getName(), is(Roles.VOLUNTEER.toString()));
    }
}
