package com.huellapositiva.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.CredentialsOrganizationMemberRequestDto;
import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.domain.Roles;
import com.huellapositiva.infrastructure.security.JwtService;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.util.Pair;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.huellapositiva.util.TestData.DEFAULT_EMAIL;
import static com.huellapositiva.util.TestData.DEFAULT_PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestData.class)
class OrganizationMemberControllerShould {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private TestData testData;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    void register_member_and_return_201_and_tokens() throws Exception {
        // GIVEN
        CredentialsOrganizationMemberRequestDto dto = new CredentialsOrganizationMemberRequestDto(DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN
        String jsonResponse = mvc.perform(post("/api/v1/organizationmember")
                .content(objectMapper.writeValueAsString(dto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse().getContentAsString();

        // THEN
        JwtResponseDto responseDto = objectMapper.readValue(jsonResponse, JwtResponseDto.class);
        Pair<String, List<String>> userDetails = jwtService.getUserDetails(responseDto.getAccessToken());
        assertThat(userDetails.getFirst()).isEqualTo(dto.getEmail());
        assertThat(userDetails.getSecond()).hasSize(1);
        assertThat(userDetails.getSecond().get(0)).isEqualTo(Roles.ORGANIZATION_MEMBER_NOT_CONFIRMED.toString());
    }
}
