package com.huellapositiva.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.*;
import com.huellapositiva.domain.model.valueobjects.ProposalCategory;
import com.huellapositiva.domain.model.valueobjects.Roles;
import com.huellapositiva.infrastructure.orm.entities.*;
import com.huellapositiva.infrastructure.orm.repository.JpaProposalRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaVolunteerRepository;
import com.huellapositiva.util.TestData;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import static com.huellapositiva.domain.model.valueobjects.ProposalDate.createClosingProposalDate;
import static com.huellapositiva.domain.model.valueobjects.ProposalStatus.*;
import static com.huellapositiva.util.TestData.*;
import static com.huellapositiva.util.TestUtils.loginAndGetJwtTokens;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    @Autowired
    private JpaProposalRepository jpaProposalRepository;

    @Autowired
    private JpaVolunteerRepository jpaVolunteerRepository;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    void persist_a_proposal() throws Exception {
        // GIVEN
        JpaContactPerson contactPerson = testData.createESALJpaContactPerson(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkESAL(contactPerson, JpaESAL.builder().id(UUID.randomUUID().toString()).name("Huella Positiva").build());
        ProposalRequestDto proposalDto = testData.buildProposalDto();
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN
        MockHttpServletResponse response = mvc.perform(multipart(REGISTER_PROPOSAL_URI)
                .file(new MockMultipartFile("file", null, "text/plain", "test data".getBytes()))
                .file(new MockMultipartFile("dto", "dto", "application/json", objectMapper.writeValueAsString(proposalDto).getBytes()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, matchesPattern("\\S+(/api/v1/proposals/)" + UUID_REGEX)))
                .andReturn().getResponse();

        // THEN
        String location = response.getHeader(HttpHeaders.LOCATION);
        String id = location.substring(location.lastIndexOf('/') + 1);
        assertThat(jpaProposalRepository.findByNaturalId(id).get().getTitle()).isEqualTo("Recogida de ropita");
    }

    @Test
    void return_400_when_date_is_invalid_when_creating_a_proposal() throws Exception {
        // GIVEN
        JpaContactPerson contactPerson = testData.createESALJpaContactPerson(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkESAL(contactPerson, JpaESAL.builder().id(UUID.randomUUID().toString()).name("Huella Positiva").build());
        String invalidStartingDate = "20-01-2021";
        ProposalRequestDto proposalDto = ProposalRequestDto.builder()
                .title("Recogida de ropita")
                .province("Santa Cruz de Tenerife")
                .town("Santa Cruz de Tenerife")
                .address("Avenida Weyler 4")
                .startingProposalDate("21-01-2021")
                .closingProposalDate("24-01-2021")
                .requiredDays("Weekends")
                .minimumAge(18)
                .maximumAge(26)
                .description("Recogida de ropa en la laguna")
                .durationInDays("1 semana")
                .startingVolunteeringDate(invalidStartingDate)
                .category(ProposalCategory.ON_SITE.toString())
                .skills(new String[][]{{"Habilidad", "Descripción"}, {"Negociación", "Saber regatear"}})
                .requirements(new String[]{"Forma física para cargar con la ropa", "Disponibilidad horaria", "Carnet de conducir"})
                .extraInfo("Es recomendable tener ganas de recoger ropa")
                .instructions("Se seleccionarán a los primeros 100 voluntarios")
                .build();
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN + THEN
        mvc.perform(multipart(REGISTER_PROPOSAL_URI)
                .file(new MockMultipartFile("file", "fileName", "text/plain", "test data".getBytes()))
                .file(new MockMultipartFile("dto", "dto", "application/json", objectMapper.writeValueAsString(proposalDto).getBytes()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void return_400_when_age_is_out_of_range() throws Exception {
        // GIVEN
        JpaContactPerson contactPerson = testData.createESALJpaContactPerson(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkESAL(contactPerson, JpaESAL.builder().id(UUID.randomUUID().toString()).name("Huella Positiva").build());
        int invalidMinimumAge = 17;
        ProposalRequestDto proposalDto = ProposalRequestDto.builder()
                .title("Recogida de ropita")
                .province("Santa Cruz de Tenerife")
                .town("Santa Cruz de Tenerife")
                .address("Avenida Weyler 4")
                .startingProposalDate("20-01-2021")
                .closingProposalDate("24-01-2021")
                .requiredDays("Weekends")
                .minimumAge(invalidMinimumAge)
                .maximumAge(26)
                .description("Recogida de ropa en la laguna")
                .durationInDays("1 semana")
                .startingVolunteeringDate("25-01-2021")
                .category(ProposalCategory.ON_SITE.toString())
                .skills(new String[][]{{"Habilidad", "Descripción"}, {"Negociación", "Saber regatear"}})
                .requirements(new String[]{"Forma física para cargar con la ropa", "Disponibilidad horaria", "Carnet de conducir"})
                .extraInfo("Es recomendable tener ganas de recoger ropa")
                .instructions("Se seleccionarán a los primeros 100 voluntarios")
                .build();
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN + THEN
        mvc.perform(multipart(REGISTER_PROPOSAL_URI)
                .file(new MockMultipartFile("file", "fileName", "text/plain", "test data".getBytes()))
                .file(new MockMultipartFile("dto", "dto", "application/json", objectMapper.writeValueAsString(proposalDto).getBytes()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void return_400_when_minimum_age_is_greater_than_maximum_age() throws Exception {
        // GIVEN
        JpaContactPerson contactPerson = testData.createESALJpaContactPerson(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkESAL(contactPerson, JpaESAL.builder().id(UUID.randomUUID().toString()).name("Huella Positiva").build());
        int invalidMinimumAge = 30;
        ProposalRequestDto proposalDto = ProposalRequestDto.builder()
                .title("Recogida de ropita")
                .province("Santa Cruz de Tenerife")
                .town("Santa Cruz de Tenerife")
                .address("Avenida Weyler 4")
                .startingProposalDate("20-01-2021")
                .closingProposalDate("24-01-2021")
                .requiredDays("Weekends")
                .minimumAge(invalidMinimumAge)
                .maximumAge(26)
                .description("Recogida de ropa en la laguna")
                .durationInDays("1 semana")
                .startingVolunteeringDate("25-01-2021")
                .category(ProposalCategory.ON_SITE.toString())
                .skills(new String[][]{{"Habilidad", "Descripción"}, {"Negociación", "Saber regatear"}})
                .requirements(new String[]{"Forma física para cargar con la ropa", "Disponibilidad horaria", "Carnet de conducir"})
                .extraInfo("Es recomendable tener ganas de recoger ropa")
                .instructions("Se seleccionarán a los primeros 100 voluntarios")
                .build();
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN + THEN
        mvc.perform(multipart(REGISTER_PROPOSAL_URI)
                .file(new MockMultipartFile("file", "fileName", "text/plain", "test data".getBytes()))
                .file(new MockMultipartFile("dto", "dto", "application/json", objectMapper.writeValueAsString(proposalDto).getBytes()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void fetch_and_return_proposal() throws Exception {
        // GIVEN
        JpaProposal proposal = testData.registerESALAndPublishedProposal();

        // WHEN
        MockHttpServletResponse fetchResponse = mvc.perform(get(FETCH_PROPOSAL_URI + proposal.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // THEN
        ProposalResponseDto proposalResponseDto = objectMapper.readValue(fetchResponse.getContentAsString(), ProposalResponseDto.class);
        assertAll(
                () -> assertThat(proposalResponseDto.getTitle()).isEqualTo("Recogida de ropita"),
                () -> assertThat(proposalResponseDto.getSkills()).isNotEmpty(),
                () -> assertThat(proposalResponseDto.getRequirements()).isNotEmpty(),
                () -> assertThatCode(() -> createClosingProposalDate(proposalResponseDto.getClosingProposalDate())).doesNotThrowAnyException()
        );
    }

    @Test
    void return_404_when_fetching_a_non_existent_proposal() throws Exception {
        // GIVEN
        int id = 999;

        // WHEN + THEN
        mvc.perform(get(FETCH_PROPOSAL_URI + id)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void return_404_when_fetching_a_not_published_or_not_finished_proposal() throws Exception {
        // GIVEN
        JpaProposal proposal = testData.registerESALAndNotPublishedProposal();

        // WHEN + THEN
        mvc.perform(get(FETCH_PROPOSAL_URI + proposal.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void fetch_a_finished_proposal_but_do_not_list_it() throws Exception {
        // GIVEN
        JpaProposal jpaProposal = testData.registerESALAndFinishedProposal();

        // WHEN
        String singleFetchResponse = mvc.perform(get(FETCH_PROPOSAL_URI + jpaProposal.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse()
                .getContentAsString();
        ProposalResponseDto singleProposal = objectMapper.readValue(singleFetchResponse, ProposalResponseDto.class);

        String paginatedFetchResponse = mvc.perform(get(FETCH_PROPOSAL_URI + "/" + 0 + "/" + 1)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse()
                .getContentAsString();
        ListedProposalsDto listedProposals = objectMapper.readValue(paginatedFetchResponse, ListedProposalsDto.class);

        // THEN
        assertAll(
                () -> assertThat(singleProposal.getId()).isEqualTo(jpaProposal.getId()),
                () -> assertThat(listedProposals.getProposals()).isEmpty()
        );
    }

    @Test
    void allow_a_volunteer_to_join() throws Exception {
        // GIVEN
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        String proposalId = testData.registerESALandPublishedProposalObject();
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN
        mvc.perform(post("/api/v1/proposals/" + proposalId + "/join")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // THEN
        JpaProposal proposal = jpaProposalRepository.findByIdWithOrganizationAndInscribedVolunteers(proposalId).get();
        assertThat(proposal.getInscribedVolunteers()).isNotEmpty();
        String volunteerId = proposal.getInscribedVolunteers().iterator().next().getId();
        assertThat(jpaVolunteerRepository.findByIdWithCredentialsAndRoles(volunteerId).get().getCredential().getEmail()).isEqualTo(DEFAULT_EMAIL);
    }

    @Test
    void return_404_when_joining_a_not_published_proposal() throws Exception {
        // GIVEN
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        String proposalId = testData.registerESALAndNotPublishedProposal().getId();
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN
        mvc.perform(post("/api/v1/proposals/" + proposalId + "/join")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON))
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
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void return_410_when_joining_a_non_existent_proposal() throws Exception {
        // GIVEN
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JpaProposal proposal = testData.registerESALAndPublishedProposal();
        proposal.setClosingProposalDate(Date.from(Instant.now().minus(1, ChronoUnit.DAYS)));
        jpaProposalRepository.save(proposal);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN
        mvc.perform(post("/api/v1/proposals/" + proposal.getId() + "/join")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isGone());
    }

    @Test
    void create_proposal_as_reviser() throws Exception {
        testData.createCredential(DEFAULT_EMAIL, UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        JpaContactPerson contactPerson = testData.createESALJpaContactPerson(DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkESAL(contactPerson, JpaESAL.builder().id(UUID.randomUUID().toString()).name("Huella Positiva").build());
        ProposalRequestDto proposalDto = testData.buildProposalDto();
        proposalDto.setEsalName("Huella Positiva");

        mvc.perform(multipart("/api/v1/proposals/reviser")
                .file(new MockMultipartFile("file", "fileName", "text/plain", "test data".getBytes()))
                .file(new MockMultipartFile("dto", "dto", "application/json", objectMapper.writeValueAsString(proposalDto).getBytes()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(header().string(HttpHeaders.LOCATION, matchesPattern("\\S+(/api/v1/proposals/)" + UUID_REGEX)))
                .andExpect(status().isCreated());

        assertThat(jpaProposalRepository.findAll()).isNotEmpty();
    }

    @Test
    void return_400_when_multipart_file_is_missing() throws Exception {
        testData.createCredential(DEFAULT_EMAIL, UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        JpaContactPerson contactPerson = testData.createESALJpaContactPerson(DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkESAL(contactPerson, JpaESAL.builder().id(UUID.randomUUID().toString()).name("Huella Positiva").build());
        ProposalRequestDto proposalDto = testData.buildProposalDto();
        proposalDto.setEsalName("Huella Positiva");

        mvc.perform(multipart("/api/v1/proposals/reviser")
                .file(new MockMultipartFile("dto", "dto", "application/json", objectMapper.writeValueAsString(proposalDto).getBytes()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void return_400_when_closing_date_is_more_than_six_months_from_now() throws Exception {
        // GIVEN
        JpaContactPerson contactPerson = testData.createESALJpaContactPerson(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkESAL(contactPerson, JpaESAL.builder().id(UUID.randomUUID().toString()).name("Huella Positiva").build());
        ProposalRequestDto proposalDto = ProposalRequestDto.builder()
                .title("Recogida de ropita")
                .province("Santa Cruz de Tenerife")
                .town("Santa Cruz de Tenerife")
                .address("Avenida Weyler 4")
                .startingProposalDate("21-08-2030")
                .closingProposalDate("24-08-2030")
                .requiredDays("Weekends")
                .minimumAge(18)
                .maximumAge(26)
                .description("Recogida de ropa en la laguna")
                .durationInDays("1 semana")
                .startingVolunteeringDate("20-08-2030")
                .category(ProposalCategory.ON_SITE.toString())
                .skills(new String[][]{{"Habilidad", "Descripción"}, {"Negociación", "Saber regatear"}})
                .requirements(new String[]{"Forma física para cargar con la ropa", "Disponibilidad horaria", "Carnet de conducir"})
                .extraInfo("Es recomendable tener ganas de recoger ropa")
                .instructions("Se seleccionarán a los primeros 100 voluntarios")
                .build();
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN + THEN
        mvc.perform(multipart(REGISTER_PROPOSAL_URI)
                .file(new MockMultipartFile("file", "fileName", "text/plain", "test data".getBytes()))
                .file(new MockMultipartFile("dto", "dto", "application/json", objectMapper.writeValueAsString(proposalDto).getBytes()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void fetch_a_paginated_list_of_published_proposals() throws Exception {
        // GIVEN
        testData.registerESALAndPublishedProposal();
        JpaESAL different_esal = testData.createJpaESAL(JpaESAL.builder().id(UUID.randomUUID().toString()).name("Different ESAL").build());
        testData.createProposal(JpaProposal.builder()
                .id(UUID.randomUUID().toString())
                .title("Limpieza de playas")
                .location(JpaLocation.builder()
                        .id(UUID.randomUUID().toString())
                        .province("Santa Cruz de Tenerife")
                        .town("Santa Cruz de Tenerife")
                        .address("Avenida Weyler 4").build())
                .esal(different_esal)
                .startingProposalDate(new SimpleDateFormat("dd-MM-yyyy").parse("20-12-2020"))
                .closingProposalDate(new SimpleDateFormat("dd-MM-yyyy").parse("24-12-2020"))
                .startingVolunteeringDate(new SimpleDateFormat("dd-MM-yyyy").parse("25-12-2020"))
                .requiredDays("Weekends")
                .minimumAge(18)
                .maximumAge(26)
                .status(testData.getJpaStatus(PUBLISHED))
                .description("Recogida de ropa en la laguna")
                .durationInDays("1 semana")
                .category(ProposalCategory.ON_SITE.toString())
                .imageUrl(testData.createMockImageUrl().toString())
                .build());

        // WHEN
        String fetchResponse1 = mvc.perform(get(FETCH_PROPOSAL_URI + "/" + 0 + "/" + 1)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse()
                .getContentAsString();
        ListedProposalsDto proposalsFetch1 = objectMapper.readValue(fetchResponse1, ListedProposalsDto.class);

        String fetchResponse2 = mvc.perform(get(FETCH_PROPOSAL_URI + "/" + 1 + "/" + 1)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse()
                .getContentAsString();
        ListedProposalsDto proposalsFetch2 = objectMapper.readValue(fetchResponse2, ListedProposalsDto.class);

        // THEN
        assertAll(
                () -> assertThat(proposalsFetch1.getProposals()).isNotEmpty(),
                () -> assertThat(proposalsFetch2.getProposals()).isNotEmpty(),
                () -> assertThat(proposalsFetch1.getProposals().get(0).getId())
                        .isNotEqualTo(proposalsFetch2.getProposals().get(0).getId())
        );
    }

    @Test
    void return_200_and_an_empty_collection_when_there_is_no_proposals_to_fetch() throws Exception {
        // WHEN
        String fetchResponse = mvc.perform(get(FETCH_PROPOSAL_URI + "/" + 0 + "/" + 1)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse()
                .getContentAsString();
        ListedProposalsDto proposalsFetch = objectMapper.readValue(fetchResponse, ListedProposalsDto.class);

        // THEN
        assertThat(proposalsFetch.getProposals()).isEmpty();
    }

    @Test
    void return_200_and_send_an_email_to_contact_person_when_submitting_a_revision_as_reviser() throws Exception {
        // GIVEN
        JpaProposal jpaProposal = testData.registerESALAndNotPublishedProposal();
        String proposalId = jpaProposal.getId();
        testData.createCredential(DEFAULT_EMAIL, UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        ProposalRevisionDto revisionDto = ProposalRevisionDto.builder()
                .hasFeedback(true)
                .feedback("Deberías profundizar más en la descripción")
                .build();

        // WHEN + THEN
        mvc.perform(post("/api/v1/proposals/revision/" + proposalId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(revisionDto))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void return_200_when_submitting_revision_as_revisor_has_feedback_but_not_feedback_text() throws Exception {
        // GIVEN
        JpaProposal jpaProposal = testData.registerESALAndNotPublishedProposal();
        String proposalId = jpaProposal.getId();
        testData.createCredential(DEFAULT_EMAIL, UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        ProposalRevisionDto revisionDto = ProposalRevisionDto.builder()
                .hasFeedback(true)
                .build();

        // WHEN + THEN
        mvc.perform(post("/api/v1/proposals/revision/" + proposalId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(revisionDto))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void return_200_when_submitting_revision_as_revisor_but_has_not_feedback() throws Exception {
        // GIVEN
        JpaProposal jpaProposal = testData.registerESALAndNotPublishedProposal();
        String proposalId = jpaProposal.getId();
        testData.createCredential(DEFAULT_EMAIL, UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        ProposalRevisionDto revisionDto = ProposalRevisionDto.builder()
                .hasFeedback(false)
                .build();

        // WHEN + THEN
        mvc.perform(post("/api/v1/proposals/revision/" + proposalId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(revisionDto))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void return_404_when_the_given_proposal_id_does_not_exist() throws Exception {
        // GIVEN
        testData.createCredential(DEFAULT_EMAIL, UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        ProposalRevisionDto revisionDto = ProposalRevisionDto.builder().feedback("Deberías profundizar más en la descripción").build();
        String nonExistingProposalId = "abcdefg";

        // WHEN + THEN
        mvc.perform(post("/api/v1/proposals/revision/" + nonExistingProposalId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(revisionDto))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void list_every_existing_proposal_that_is_not_inadequate_as_reviser() throws Exception {
        // GIVEN
        testData.createCredential(DEFAULT_EMAIL, UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.registerESALAndFinishedProposal();
        JpaESAL different_esal = testData.createJpaESAL(JpaESAL.builder().id(UUID.randomUUID().toString()).name("Different ESAL").build());
        testData.createProposal(JpaProposal.builder()
                .id(UUID.randomUUID().toString())
                .title("Limpieza de playas")
                .location(JpaLocation.builder()
                        .id(UUID.randomUUID().toString())
                        .province("Santa Cruz de Tenerife")
                        .town("Santa Cruz de Tenerife")
                        .address("Avenida Weyler 4").build())
                .esal(different_esal)
                .startingProposalDate(new SimpleDateFormat("dd-MM-yyyy").parse("20-12-2020"))
                .closingProposalDate(new SimpleDateFormat("dd-MM-yyyy").parse("24-12-2020"))
                .startingVolunteeringDate(new SimpleDateFormat("dd-MM-yyyy").parse("25-12-2020"))
                .requiredDays("Weekends")
                .minimumAge(18)
                .maximumAge(26)
                .status(testData.getJpaStatus(REVIEW_PENDING))
                .description("Recogida de ropa en la laguna")
                .durationInDays("1 semana")
                .category(ProposalCategory.ON_SITE.toString())
                .imageUrl(testData.createMockImageUrl().toString())
                .build());

        // WHEN
        String fetchResponse = mvc.perform(get(FETCH_PROPOSAL_URI + "/" + 0 + "/" + 5 + "/reviser")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse()
                .getContentAsString();
        ListedProposalsDto listedProposals = objectMapper.readValue(fetchResponse, ListedProposalsDto.class);

        assertThat(listedProposals.getProposals().size()).isEqualTo(2);
    }


    @Test
    void return_200_and_a_list_of_volunteers_from_a_proposal_as_reviser() throws Exception {

        // GIVEN
        String proposalId = testData.registerESALAndProposalWithInscribedVolunteers().getId();
        testData.createCredential("revisor@huellapositiva.com", UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, "revisor@huellapositiva.com", DEFAULT_PASSWORD);

        MockHttpServletResponse fetchResponse = mvc.perform(get(FETCH_PROPOSAL_URI + proposalId + "/volunteers")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();


        ArrayList<VolunteerDto> listedVolunteers = objectMapper.readValue(fetchResponse.getContentAsString(), ArrayList.class);
        assertThat(listedVolunteers.size()).isEqualTo(1);

    }

    @Test
    void return_404_when_proposal_not_found() throws Exception{

        // GIVEN
        String proposalId = "5be08393-7a09-465a-931f-239b168c4642";
        testData.createCredential(DEFAULT_EMAIL, UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN
        mvc.perform(get(FETCH_PROPOSAL_URI + proposalId + "/volunteers")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void return_200_and_the_proposal_with_volunteers_as_reviser() throws Exception {

        // GIVEN
        String proposalId = testData.registerESALAndProposalWithInscribedVolunteers().getId();
        testData.createCredential("revisor@huellapositiva.com", UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, "revisor@huellapositiva.com", DEFAULT_PASSWORD);

        MockHttpServletResponse fetchResponse = mvc.perform(get(FETCH_PROPOSAL_URI + proposalId + "/proposal")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        ProposalResponseDto proposalDto = objectMapper.readValue(fetchResponse.getContentAsString(), ProposalResponseDto.class);
        assertThat(proposalDto.getId()).isEqualTo(proposalId);
        assertThat(proposalDto.getInscribedVolunteers().size()).isPositive();

    }

    @Test
    void return_404_when_proposal_with_volunteers_not_found() throws Exception{

        // GIVEN
        String proposalId = "999";
        testData.createCredential(DEFAULT_EMAIL, UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN
        mvc.perform(get(FETCH_PROPOSAL_URI + proposalId + "/proposal")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void return_200_when_a_proposal_is_successfully_cancelled() throws Exception {
        //GIVEN
        String proposalId = testData.registerESALAndProposalWithInscribedVolunteers().getId();
        testData.createCredential("revisor@huellapositiva.com", UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, "revisor@huellapositiva.com", DEFAULT_PASSWORD);

        //WHEN + THEN
        MockHttpServletResponse fetchResponse = mvc.perform(post(FETCH_PROPOSAL_URI + proposalId + "/cancel")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
/*
        MockHttpServletResponse fetchResponseGet = mvc.perform(get(FETCH_PROPOSAL_URI + proposalId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        ProposalResponseDto proposalDto = objectMapper.readValue(fetchResponse.getContentAsString(), ProposalResponseDto.class);
        assertThat(proposalDto.getStatus()).isEqualTo(CANCELLED); */

    }
}
