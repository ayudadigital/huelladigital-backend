package com.huellapositiva.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.application.dto.ESALRequestDto;
import com.huellapositiva.domain.model.valueobjects.Roles;
import com.huellapositiva.infrastructure.orm.entities.Organization;
import com.huellapositiva.infrastructure.orm.entities.OrganizationMember;
import com.huellapositiva.infrastructure.orm.repository.JpaOrganizationRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestData.class)
class ESALControllerShould {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private TestData testData;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JpaOrganizationRepository jpaOrganizationRepository;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    void create_an_organization_and_update_member_joined_organization() throws Exception {
        testData.createESALMember(DEFAULT_EMAIL, DEFAULT_PASSWORD);
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
    void create_an_organization_as_admin() throws Exception {
        testData.createCredential(DEFAULT_EMAIL, UUID.randomUUID(), DEFAULT_PASSWORD, Roles.ADMIN);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        mvc.perform(post("/api/v1/esal/admin")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(new ESALRequestDto("Huella positiva")))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk());

        assertThat(jpaOrganizationRepository.findByName("Huella positiva")).isPresent();
    }

    @Test
    void allow_members_to_delete_their_organization() throws Exception {
        OrganizationMember organizationMember = testData.createESALMember(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        Integer organizationId = testData.createAndLinkESAL(organizationMember, Organization.builder().name(DEFAULT_ESAL).build());

        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        mvc.perform(delete("/api/v1/esal/" + organizationId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk());

        assertThat(jpaOrganizationRepository.findById(organizationId)).isEmpty();
    }

    @Test
    void not_allow_to_create_an_organization_when_member_already_has_one() throws Exception {
        OrganizationMember member = testData.createESALMember(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkESAL(member, Organization.builder().name("Huella Negativa").build());
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
    void return_409_when_organization_is_already_taken() throws Exception {
        testData.createESALMember(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createESAL(Organization.builder().name("Huella Positiva").build());
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
    void return_401_when_a_user_attempts_to_delete_an_organization_that_does_not_belong_to() throws Exception {
        OrganizationMember organizationMember = testData.createESALMember(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkESAL(organizationMember, Organization.builder().name("Huella Positiva").build());
        Integer secondOrganizationId = testData.createESAL(Organization.builder().name("Huella Negativa").build());

        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        mvc.perform(delete("/api/v1/esal/" + secondOrganizationId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
