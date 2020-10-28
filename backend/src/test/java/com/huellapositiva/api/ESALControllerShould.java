package com.huellapositiva.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.ESALRequestDto;
import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.domain.model.valueobjects.Roles;
import com.huellapositiva.infrastructure.orm.entities.JpaContactPerson;
import com.huellapositiva.infrastructure.orm.entities.JpaESAL;
import com.huellapositiva.infrastructure.orm.repository.JpaESALRepository;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static com.huellapositiva.util.TestData.*;
import static com.huellapositiva.util.TestUtils.loginAndGetJwtTokens;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"aws.paramstore.enabled=false"})
@AutoConfigureMockMvc
@Import(TestData.class)
class ESALControllerShould {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private TestData testData;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JpaESALRepository jpaESALRepository;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    void create_an_ESAL_and_update_member_joined_ESAL() throws Exception {
        testData.createESALJpaContactPerson(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        mvc.perform(post("/api/v1/esal")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(new ESALRequestDto("Huella positiva")))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void create_an_ESAL_as_a_not_confirmed_contact_person() throws Exception {
        testData.createESALJpaContactPerson(DEFAULT_EMAIL, DEFAULT_PASSWORD, Roles.CONTACT_PERSON_NOT_CONFIRMED);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        mvc.perform(post("/api/v1/esal")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(new ESALRequestDto("Huella positiva")))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void create_an_ESAL_as_reviser() throws Exception {
        testData.createCredential(DEFAULT_EMAIL, UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        mvc.perform(post("/api/v1/esal/reviser")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(new ESALRequestDto("Huella positiva")))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk());

        assertThat(jpaESALRepository.findByName("Huella positiva")).isPresent();
    }

    @Test
    void allow_members_to_delete_their_ESAL() throws Exception {
        JpaContactPerson contactPerson = testData.createESALJpaContactPerson(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        String esalId = testData.createAndLinkESAL(contactPerson, JpaESAL.builder().id(UUID.randomUUID().toString()).name(DEFAULT_ESAL).build());

        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        mvc.perform(delete("/api/v1/esal/" + esalId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk());

        assertThat(jpaESALRepository.findByNaturalId(esalId)).isEmpty();
    }

    @Test
    void not_allow_to_create_an_ESAL_when_member_already_has_one() throws Exception {
        JpaContactPerson member = testData.createESALJpaContactPerson(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkESAL(member, JpaESAL.builder().id(UUID.randomUUID().toString()).name("Huella Negativa").build());
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        mvc.perform(post("/api/v1/esal")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(new ESALRequestDto("Huella positiva")))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isPreconditionFailed());
    }

    @Test
    void return_409_when_ESAL_is_already_taken() throws Exception {
        testData.createESALJpaContactPerson(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createJpaESAL(JpaESAL.builder().id(UUID.randomUUID().toString()).name("Huella Positiva").build());
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        mvc.perform(post("/api/v1/esal")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(new ESALRequestDto("Huella Positiva")))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void return_403_when_a_user_attempts_to_delete_an_ESAL_that_does_not_belong_to() throws Exception {
        JpaContactPerson contactPerson = testData.createESALJpaContactPerson(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkESAL(contactPerson, JpaESAL.builder().id(UUID.randomUUID().toString()).name("Huella Positiva").build());
        String secondESALId = testData.createJpaESAL(JpaESAL.builder().id(UUID.randomUUID().toString()).name("Huella Negativa").build()).getId();

        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        mvc.perform(delete("/api/v1/esal/" + secondESALId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
