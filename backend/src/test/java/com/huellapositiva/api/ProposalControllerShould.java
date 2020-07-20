package com.huellapositiva.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.application.dto.ProposalRequestDto;
import com.huellapositiva.application.dto.ProposalResponseDto;
import com.huellapositiva.domain.Roles;
import com.huellapositiva.infrastructure.orm.model.Organization;
import com.huellapositiva.infrastructure.orm.model.OrganizationEmployee;
import com.huellapositiva.infrastructure.orm.model.Proposal;
import com.huellapositiva.infrastructure.orm.repository.JpaProposalRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaVolunteerRepository;
import com.huellapositiva.util.TestData;
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
import static com.huellapositiva.util.TestUtils.registerProposalRequest;
import static org.assertj.core.api.Assertions.assertThat;
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

    @Autowired
    private JpaVolunteerRepository jpaVolunteerRepository;

    @Test
    void create_an_organization_and_update_employee_joined_organization() throws Exception {
        // GIVEN
        OrganizationEmployee organizationEmployee = testData.createOrganizationEmployee(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkOrganization(organizationEmployee, Organization.builder().name("Huella Positiva").build());
        ProposalRequestDto proposalDto = testData.buildUnpublishedProposalDto();
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
        assertThat(jpaProposalRepository.findAll()).isNotEmpty();
    }

    @Test
    void return_422_when_an_employee_not_confirmed_tries_to_create_a_proposal() throws Exception {
        // GIVEN
        OrganizationEmployee organizationEmployee = testData.createOrganizationEmployee(DEFAULT_EMAIL, DEFAULT_PASSWORD, Roles.ORGANIZATION_EMPLOYEE_NOT_CONFIRMED);
        testData.createAndLinkOrganization(organizationEmployee, Organization.builder().name("Huella Positiva").build());
        ProposalRequestDto proposalDto = testData.buildUnpublishedProposalDto();
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN + THEN
        mvc.perform(post(REGISTER_PROPOSAL_URI)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(proposalDto))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity());
    }


    @Test
    void fetch_and_return_proposal() throws Exception {
        // GIVEN
        OrganizationEmployee organizationEmployee = testData.createOrganizationEmployee(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkOrganization(organizationEmployee, Organization.builder().name("Huella Positiva").build());
        ProposalRequestDto proposalDto = testData.buildPublishedProposalDto();
        String accessToken = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD).getAccessToken();
        registerProposalRequest(mvc, accessToken, proposalDto);
        Integer proposalId = jpaProposalRepository.findAll().get(0).getId();

        // WHEN
        MockHttpServletResponse fetchResponse = mvc.perform(get(FETCH_PROPOSAL_URI + proposalId)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // THEN
        ProposalResponseDto proposalResponseDto = objectMapper.readValue(fetchResponse.getContentAsString(), ProposalResponseDto.class);
        assertThat(proposalResponseDto.getTitle()).isEqualTo("Recogida de ropita");
    }

    @Test
    void return_404_when_fetching_a_non_existent_proposal() throws Exception {
        // GIVEN
        int id = 999;

        // WHEN + THEN
        mvc.perform(get(FETCH_PROPOSAL_URI + id)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void return_412_when_fetching_a_not_published_proposal() throws Exception {
        // GIVEN
        OrganizationEmployee organizationEmployee = testData.createOrganizationEmployee(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkOrganization(organizationEmployee, Organization.builder().name("Huella Positiva").build());
        ProposalRequestDto proposalDto = testData.buildUnpublishedProposalDto();
        String accessToken = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD).getAccessToken();
        registerProposalRequest(mvc, accessToken, proposalDto);

        Integer proposalId = jpaProposalRepository.findAll().get(0).getId();

        // WHEN
        mvc.perform(get(FETCH_PROPOSAL_URI + proposalId)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isPreconditionFailed());
    }

    @Test
    void allow_a_volunteer_to_join() throws Exception {
        // GIVEN
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        Integer proposalId = testData.registerOrganizationAndPublishedProposal().getId();
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN
        mvc.perform(post("/api/v1/proposals/" + proposalId + "/join")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(APPLICATION_JSON)
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk());

        // THEN
        Proposal proposal = jpaProposalRepository.findById(proposalId).get();
        assertThat(proposal.getInscribedVolunteers()).isNotEmpty();
        Integer volunteerId = proposal.getInscribedVolunteers().iterator().next().getId();
        assertThat(jpaVolunteerRepository.findByIdWithCredentialsAndRoles(volunteerId).get().getCredential().getEmail()).isEqualTo(DEFAULT_EMAIL);
    }

    @Test
    void return_412_when_joining_a_not_published_proposal() throws Exception {
        // GIVEN
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        Integer proposalId = testData.registerOrganizationAndNotPublishedProposal().getId();
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN
        mvc.perform(post("/api/v1/proposals/" + proposalId + "/join")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(APPLICATION_JSON)
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isPreconditionFailed());
    }

    @Test
    void return_404_when_joining_a_non_existent_proposal() throws Exception {
        // GIVEN
        int id = 999;
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        Integer proposalId = testData.registerOrganizationAndNotPublishedProposal().getId();
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN + THEN
        mvc.perform(post(FETCH_PROPOSAL_URI + id + "/join")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(APPLICATION_JSON)
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void return_422_when_a_volunteer_not_confirmed_tries_to_join_a_proposal() throws Exception {
        // GIVEN
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD, Roles.VOLUNTEER_NOT_CONFIRMED);
        Integer proposalId = testData.registerOrganizationAndPublishedProposal().getId();
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN + THEN
        mvc.perform(post("/api/v1/proposals/" + proposalId + "/join")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(APPLICATION_JSON)
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity());
    }
}
