package com.huellapositiva.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.application.dto.ProposalRequestDto;
import com.huellapositiva.application.dto.ProposalResponseDto;
import com.huellapositiva.infrastructure.orm.model.Organization;
import com.huellapositiva.infrastructure.orm.model.OrganizationEmployee;
import com.huellapositiva.infrastructure.orm.repository.JpaOrganizationEmployeeRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaProposalRepository;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import static com.huellapositiva.util.TestData.DEFAULT_EMAIL;
import static com.huellapositiva.util.TestData.DEFAULT_PASSWORD;
import static com.huellapositiva.util.TestUtils.loginAndGetJwtTokens;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestData.class)
class ProposalControllerShould {

    private static final String REGISTER_PROPOSAL_URI = "/api/v1/proposals/register";

    private static final String FETCH_PROPOSAL_URI = "/api/v1/proposals/";

    private static final ObjectMapper objectMapper = new ObjectMapper();


    @Autowired
    private TestData testData;

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Autowired
    private JpaProposalRepository jpaProposalRepository;

    @Test
    void create_an_organization_and_update_employee_joined_organization() throws Exception {
        // GIVEN
        OrganizationEmployee organizationEmployee = testData.createOrganizationEmployee(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkOrganization(organizationEmployee, Organization.builder().name("Huella Positiva").build());
        ProposalRequestDto proposalDto = ProposalRequestDto.builder()
                .title("Recogida de alimentos")
                .province("Santa Cruz de Tenerife")
                .town("Santa Cruz de Tenerife")
                .address("Avenida Weyler 4")
                .expirationDate("24-08-2020")
                .requiredDays("Weekends")
                .minimumAge(18)
                .maximumAge(26)
                .build();
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN
        mvc.perform(post(REGISTER_PROPOSAL_URI)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(proposalDto))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk());

        // THEN
        assertNotEquals(0, jpaProposalRepository.findAll().size());
    }

    @Test
    void fetch_and_return_proposal() throws Exception {
        // GIVEN
        OrganizationEmployee organizationEmployee = testData.createOrganizationEmployee(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkOrganization(organizationEmployee, Organization.builder().name("Huella Positiva").build());
        ProposalRequestDto proposalDto = ProposalRequestDto.builder()
                .title("Recogida de ropita")
                .province("Santa Cruz de Tenerife")
                .town("Santa Cruz de Tenerife")
                .address("Avenida Weyler 4")
                .expirationDate("24-08-2020")
                .requiredDays("Weekends")
                .minimumAge(18)
                .maximumAge(26)
                .published(true)
                .build();
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        mvc.perform(post(REGISTER_PROPOSAL_URI)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(proposalDto))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk());

        Integer proposalId = jpaProposalRepository.findAll().get(0).getId();

        // WHEN
        MockHttpServletResponse fetchResponse = mvc.perform(get(FETCH_PROPOSAL_URI + proposalId)
                .content(objectMapper.writeValueAsString(proposalDto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // THEN
        ProposalResponseDto proposalResponseDto = objectMapper.readValue(fetchResponse.getContentAsString(), ProposalResponseDto.class);
        assertThat(proposalResponseDto.getTitle()).isEqualTo("Recogida de ropita");
    }
}
