package com.huellapositiva.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.application.dto.ProposalRequestDto;
import com.huellapositiva.application.dto.ProposalResponseDto;
import com.huellapositiva.domain.Roles;
import com.huellapositiva.infrastructure.orm.model.Organization;
import com.huellapositiva.infrastructure.orm.model.OrganizationMember;
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

import java.util.UUID;

import static com.huellapositiva.util.TestData.*;
import static com.huellapositiva.util.TestUtils.loginAndGetJwtTokens;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestData.class)
class ProposalControllerShould {

    private static final String REGISTER_PROPOSAL_URI = "/api/v1/proposals";

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
    void create_an_organization_and_update_member_joined_organization() throws Exception {
        // GIVEN
        OrganizationMember organizationMember = testData.createOrganizationMember(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkOrganization(organizationMember, Organization.builder().name("Huella Positiva").build());
        ProposalRequestDto proposalDto = testData.buildUnpublishedProposalDto();
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN
        MockHttpServletResponse response = mvc.perform(post(REGISTER_PROPOSAL_URI)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(proposalDto))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, matchesPattern("\\S+(/api/v1/proposals/)\\d+")))
                .andReturn().getResponse();

        // THEN
        String location = response.getHeader(HttpHeaders.LOCATION);
        int id = Integer.parseInt(location.substring(location.lastIndexOf('/') + 1));
        assertThat(jpaProposalRepository.findById(id).get().getTitle()).isEqualTo("Recogida de ropita");
    }

    @Test
    void fetch_and_return_proposal() throws Exception {
        // GIVEN
        Proposal proposal = testData.registerOrganizationAndPublishedProposal();

        // WHEN
        MockHttpServletResponse fetchResponse = mvc.perform(get(FETCH_PROPOSAL_URI + proposal.getId())
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
    void return_404_when_fetching_a_not_published_proposal() throws Exception {
        // GIVEN
        Proposal proposal = testData.registerOrganizationAndNotPublishedProposal();

        // WHEN + THEN
        mvc.perform(get(FETCH_PROPOSAL_URI + proposal.getId())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isNotFound());
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
        assertThat(proposal.getJoinedVolunteers()).isNotEmpty();
        Integer volunteerId = proposal.getJoinedVolunteers().iterator().next().getId();
        assertThat(jpaVolunteerRepository.findByIdWithCredentialsAndRoles(volunteerId).get().getCredential().getEmail()).isEqualTo(DEFAULT_EMAIL);
    }

    @Test
    void return_404_when_joining_a_not_published_proposal() throws Exception {
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
                .andExpect(status().isNotFound());
    }

    @Test
    void return_404_when_joining_a_non_existent_proposal() throws Exception {
        // GIVEN
        int id = 999;
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
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
    void create_proposal_as_admin() throws Exception {
        testData.createCredential(DEFAULT_EMAIL, UUID.randomUUID(), DEFAULT_PASSWORD, Roles.ADMIN);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        OrganizationMember organizationMember = testData.createOrganizationMember(DEFAULT_ORGANIZATION_MEMBER_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkOrganization(organizationMember, Organization.builder().name("Huella Positiva").build());
        ProposalRequestDto proposalRequestDto = testData.buildProposalDto(true);
        proposalRequestDto.setOrganizationName("Huella Positiva");

        mvc.perform(post("/api/v1/proposals/admin")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(proposalRequestDto))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(header().string(HttpHeaders.LOCATION, matchesPattern("\\S+(/api/v1/proposals/)\\d+")))
                .andExpect(status().isCreated());

        assertThat(jpaProposalRepository.findAll()).isNotEmpty();
    }
}
