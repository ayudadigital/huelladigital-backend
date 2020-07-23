package com.huellapositiva.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.application.dto.OrganizationRequestDto;
import com.huellapositiva.domain.model.valueobjects.Roles;
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

import static com.huellapositiva.util.TestData.DEFAULT_EMAIL;
import static com.huellapositiva.util.TestData.DEFAULT_PASSWORD;
import static com.huellapositiva.util.TestUtils.loginAndGetJwtTokens;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestData.class)
class OrganizationControllerShould {

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
        testData.createOrganizationMember(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        mvc.perform(post("/api/v1/organizations")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(new OrganizationRequestDto("Huella positiva")))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void create_an_organization_as_admin() throws Exception {
        testData.createCredential(DEFAULT_EMAIL, UUID.randomUUID(), DEFAULT_PASSWORD, Roles.ADMIN);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        mvc.perform(post("/api/v1/organizations/admin")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(new OrganizationRequestDto("Huella positiva")))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk());

        assertThat(jpaOrganizationRepository.findByName("Huella positiva")).isPresent();
    }
}
