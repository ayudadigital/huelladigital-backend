package com.huellapositiva.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.*;
import com.huellapositiva.application.exception.UserNotFoundException;
import com.huellapositiva.domain.model.entities.Proposal;
import com.huellapositiva.domain.model.valueobjects.*;
import com.huellapositiva.domain.repository.ProposalRepository;
import com.huellapositiva.domain.service.ProposalService;
import com.huellapositiva.infrastructure.orm.entities.*;
import com.huellapositiva.infrastructure.orm.repository.JpaProposalRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaVolunteerRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaVolunteersProposalsRepository;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import javax.persistence.EntityNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.huellapositiva.domain.model.valueobjects.ProposalDate.createClosingProposalDate;
import static com.huellapositiva.domain.model.valueobjects.ProposalStatus.*;
import static com.huellapositiva.util.TestData.*;
import static com.huellapositiva.util.TestUtils.loginAndGetJwtTokens;
import static java.lang.String.format;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestData.class)
class ProposalControllerShould {

    private static final String PROPOSALS_BASE_URI = "/api/v1/proposals";

    private static final String FETCH_PROPOSAL_URI = "/api/v1/proposals/";

    @Autowired
    private TestData testData;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JpaProposalRepository jpaProposalRepository;

    @Autowired
    private JpaVolunteerRepository jpaVolunteerRepository;

    @Autowired
    private JpaVolunteersProposalsRepository jpaVolunteersProposalsRepository;

    @Autowired
    private ProposalRepository proposalRepository;

    @Autowired
    private ProposalService proposalService;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    void return_404_when_finishing_a_non_existing_proposal() throws Exception {
        // GIVEN
        JpaProposal publishedProposal = testData.registerESALAndProposal(PUBLISHED);
        publishedProposal.setClosingProposalDate(Date.from(Instant.now().minus(1, DAYS)));
        jpaProposalRepository.save(publishedProposal);

        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);


        // WHEN + THEN
        mvc.perform(put(FETCH_PROPOSAL_URI + (int)(Math.random()*5) + "/status/finished")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void return_409_when_inadequate_criteria_to_change_proposal_status_to_finished() throws Exception {
        // GIVEN
        JpaProposal publishedProposal = testData.registerESALAndProposal(CANCELLED);
        publishedProposal.setClosingProposalDate(Date.from(Instant.now().minus(1, DAYS)));
        jpaProposalRepository.save(publishedProposal);

        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);


        // WHEN + THEN
        mvc.perform(put(FETCH_PROPOSAL_URI + publishedProposal.getId() + "/status/finished")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(mvcResult -> {
                    JpaProposal fetchedProposal = jpaProposalRepository.findByNaturalId(publishedProposal.getId()).get();
                    assertThat(fetchedProposal.getStatus().getName()).isEqualTo("cancelled");
                });
    }

    @Test
    void return_200_when_adequate_criteria_to_change_proposal_status_to_finished() throws Exception {
        // GIVEN
        JpaProposal publishedProposal = testData.registerESALAndProposal(PUBLISHED);
        publishedProposal.setClosingProposalDate(Date.from(Instant.now().minus(1, DAYS)));
        jpaProposalRepository.save(publishedProposal);

        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);


        // WHEN + THEN
        mvc.perform(put(FETCH_PROPOSAL_URI + publishedProposal.getId() + "/status/finished")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(mvcResult -> {
                    JpaProposal fetchedProposal = jpaProposalRepository.findByNaturalId(publishedProposal.getId()).get();
                    assertThat(fetchedProposal.getStatus().getName()).isEqualTo("finished");
                });
    }

    @Test
    void persist_a_proposal() throws Exception {
        // GIVEN
        JpaContactPerson contactPerson = testData.createESALJpaContactPerson(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkESAL(contactPerson, testData.buildJpaESAL("Huella Positiva"));
        ProposalRequestDto proposalDto = testData.buildProposalDto();
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN
        MockHttpServletResponse response = mvc.perform(multipart(PROPOSALS_BASE_URI)
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
        JpaContactPerson contactPerson = testData.createESALJpaContactPerson(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkESAL(contactPerson, testData.buildJpaESAL("Huella Positiva"));
        String invalidStartingDate = "20-01-2021";
        ProposalRequestDto proposalDto = ProposalRequestDto.builder()
                .title("Recogida de ropita")
                .province("Santa Cruz de Tenerife")
                .town("Santa Cruz de Tenerife")
                .address("Avenida Weyler 4")
                .island("Tenerife")
                .zipCode("12345")
                .startingProposalDate("21-01-2021")
                .closingProposalDate("24-01-2021")
                .requiredDays("Weekends")
                .minimumAge(18)
                .maximumAge(26)
                .description("Recogida de ropa en la laguna")
                .durationInDays("1 semana")
                .startingVolunteeringDate(invalidStartingDate)
                .category(ProposalCategory.ON_SITE.toString())
                .skills(List.of(
                        new SkillDto("Habilidad", "Descripción"),
                        new SkillDto("Negociación", "Saber regatear")))
                .requirements(List.of(
                        "Forma física para cargar con la ropa",
                        "Disponibilidad horaria",
                        "Carnet de conducir"))
                .extraInfo("Es recomendable tener ganas de recoger ropa")
                .instructions("Se seleccionarán a los primeros 100 voluntarios")
                .build();
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN + THEN
        mvc.perform(multipart(PROPOSALS_BASE_URI)
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
        JpaContactPerson contactPerson = testData.createESALJpaContactPerson(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkESAL(contactPerson, testData.buildJpaESAL("Huella Positiva"));
        int invalidMinimumAge = 17;
        ProposalRequestDto proposalDto = ProposalRequestDto.builder()
                .title("Recogida de ropita")
                .province("Santa Cruz de Tenerife")
                .town("Santa Cruz de Tenerife")
                .address("Avenida Weyler 4")
                .island("Tenerife")
                .zipCode("12345")
                .startingProposalDate("20-01-2021")
                .closingProposalDate("24-01-2021")
                .requiredDays("Weekends")
                .minimumAge(invalidMinimumAge)
                .maximumAge(26)
                .description("Recogida de ropa en la laguna")
                .durationInDays("1 semana")
                .startingVolunteeringDate("25-01-2021")
                .category(ProposalCategory.ON_SITE.toString())
                .skills(List.of(
                        new SkillDto("Habilidad", "Descripción"),
                        new SkillDto("Negociación", "Saber regatear")))
                .requirements(List.of(
                        "Forma física para cargar con la ropa",
                        "Disponibilidad horaria",
                        "Carnet de conducir"))
                .extraInfo("Es recomendable tener ganas de recoger ropa")
                .instructions("Se seleccionarán a los primeros 100 voluntarios")
                .build();
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN + THEN
        mvc.perform(multipart(PROPOSALS_BASE_URI)
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
        JpaContactPerson contactPerson = testData.createESALJpaContactPerson(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkESAL(contactPerson, testData.buildJpaESAL("Huella Positiva"));
        int invalidMinimumAge = 30;
        ProposalRequestDto proposalDto = ProposalRequestDto.builder()
                .title("Recogida de ropita")
                .province("Santa Cruz de Tenerife")
                .town("Santa Cruz de Tenerife")
                .address("Avenida Weyler 4")
                .island("Tenerife")
                .zipCode("12345")
                .startingProposalDate("20-01-2021")
                .closingProposalDate("24-01-2021")
                .requiredDays("Weekends")
                .minimumAge(invalidMinimumAge)
                .maximumAge(26)
                .description("Recogida de ropa en la laguna")
                .durationInDays("1 semana")
                .startingVolunteeringDate("25-01-2021")
                .category(ProposalCategory.ON_SITE.toString())
                .skills(List.of(
                        new SkillDto("Habilidad", "Descripción"),
                        new SkillDto("Negociación", "Saber regatear")))
                .requirements(List.of(
                        "Forma física para cargar con la ropa",
                        "Disponibilidad horaria",
                        "Carnet de conducir"))
                .extraInfo("Es recomendable tener ganas de recoger ropa")
                .instructions("Se seleccionarán a los primeros 100 voluntarios")
                .build();
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN + THEN
        mvc.perform(multipart(PROPOSALS_BASE_URI)
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
        JpaProposal proposal = testData.registerESALAndProposal(PUBLISHED);

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
        JpaProposal proposal = testData.registerESALAndProposal(REVIEW_PENDING);

        // WHEN + THEN
        mvc.perform(get(FETCH_PROPOSAL_URI + proposal.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void fetch_a_finished_proposal_but_do_not_list_it() throws Exception {
        // GIVEN
        JpaProposal jpaProposal = testData.registerESALAndProposal(FINISHED);

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
        String proposalId = testData.registerESALAndProposal(REVIEW_PENDING).getId();
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
        JpaProposal proposal = testData.registerESALAndProposal(ENROLLMENT_CLOSED);
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

        JpaContactPerson contactPerson = testData.createESALJpaContactPerson(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkESAL(contactPerson, testData.buildJpaESAL("Huella Positiva"));
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
        JpaContactPerson contactPerson = testData.createESALJpaContactPerson(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkESAL(contactPerson, testData.buildJpaESAL("Huella Positiva"));
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
        JpaContactPerson contactPerson = testData.createESALJpaContactPerson(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkESAL(contactPerson, testData.buildJpaESAL("Huella Positiva"));
        ProposalRequestDto proposalDto = ProposalRequestDto.builder()
                .title("Recogida de ropita")
                .province("Santa Cruz de Tenerife")
                .town("Santa Cruz de Tenerife")
                .address("Avenida Weyler 4")
                .zipCode("12345")
                .island("Tenerife")
                .startingProposalDate("21-08-2030")
                .closingProposalDate("24-08-2030")
                .requiredDays("Weekends")
                .minimumAge(18)
                .maximumAge(26)
                .description("Recogida de ropa en la laguna")
                .durationInDays("1 semana")
                .startingVolunteeringDate("20-08-2030")
                .category(ProposalCategory.ON_SITE.toString())
                .skills(List.of(
                        new SkillDto("Habilidad", "Descripción"),
                        new SkillDto("Negociación", "Saber regatear")))
                .requirements(List.of(
                        "Forma física para cargar con la ropa",
                        "Disponibilidad horaria",
                        "Carnet de conducir"))
                .extraInfo("Es recomendable tener ganas de recoger ropa")
                .instructions("Se seleccionarán a los primeros 100 voluntarios")
                .build();
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN + THEN
        mvc.perform(multipart(PROPOSALS_BASE_URI)
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
        testData.registerESALAndProposal(PUBLISHED);
        JpaESAL different_esal = testData.createJpaESAL(testData.buildJpaESAL("Different ESAL"));
        testData.createProposal(JpaProposal.builder()
                .id(Id.newId().toString())
                .title("Limpieza de playas")
                .location(JpaLocation.builder()
                        .id(Id.newId().toString())
                        .province("Santa Cruz de Tenerife")
                        .town("Santa Cruz de Tenerife")
                        .address("Avenida Weyler 4")
                        .zipCode("12345")
                        .island("Tenerife").build())
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

    private static Stream<ProposalRevisionDto> provideProposalRevisionDTO() {
        return Stream.of(
                new ProposalRevisionDto("Deberías profundizar más en la descripción"),
                new ProposalRevisionDto(null)
        );
    }

    @ParameterizedTest
    @MethodSource("provideProposalRevisionDTO")
    void return_200_and_send_an_email_to_contact_person_when_submitting_a_revision_as_reviser(ProposalRevisionDto revisionDto) throws Exception {
        // GIVEN
        JpaProposal jpaProposal = testData.registerESALAndProposal(REVIEW_PENDING);
        String proposalId = jpaProposal.getId();
        testData.createCredential(DEFAULT_EMAIL, UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN + THEN
        mvc.perform(post(format("/api/v1/proposals/%s/revision/", proposalId))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(revisionDto))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        JpaProposal jpaProposalWithStatusChanged = jpaProposalRepository.findByNaturalId(proposalId)
                .orElseThrow(() -> new UserNotFoundException("Proposal not found. Account ID: " + proposalId));
        assertThat(jpaProposalWithStatusChanged.getStatus().getId()).isEqualTo(CHANGES_REQUESTED.getId());
    }

    @Test
    void return_404_when_the_given_proposal_id_does_not_review_pending() throws Exception {
        // GIVEN
        JpaProposal jpaProposal = testData.registerESALAndProposal(PUBLISHED);
        String proposalId = jpaProposal.getId();
        testData.createCredential(DEFAULT_EMAIL, UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        ProposalRevisionDto revisionDto = new ProposalRevisionDto(null);

        // WHEN + THEN
        mvc.perform(post(format("/api/v1/proposals/%s/revision/", proposalId))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(revisionDto))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void return_404_when_the_given_proposal_id_does_not_exist() throws Exception {
        // GIVEN
        testData.createCredential(DEFAULT_EMAIL, UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        ProposalRevisionDto revisionDto = new ProposalRevisionDto("Deberías profundizar más en la descripción");
        String nonExistingProposalId = "abcdefg";

        // WHEN + THEN
        mvc.perform(post(format("/api/v1/proposals/%s/revision/", nonExistingProposalId))
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
        testData.registerESALAndProposal(FINISHED);
        JpaESAL different_esal = testData.createJpaESAL(testData.buildJpaESAL("Different ESAL"));
        testData.createProposal(JpaProposal.builder()
                .id(UUID.randomUUID().toString())
                .title("Limpieza de playas")
                .location(JpaLocation.builder()
                        .id(UUID.randomUUID().toString())
                        .province("Santa Cruz de Tenerife")
                        .town("Santa Cruz de Tenerife")
                        .address("Avenida Weyler 4")
                        .zipCode("12345")
                        .island("Tenerife").build())
                .esal(different_esal)
                .startingProposalDate(Date.from(Instant.now().plus(1, DAYS)))
                .closingProposalDate(Date.from(Instant.now().plus(5, DAYS)))
                .startingVolunteeringDate(Date.from(Instant.now().plus(6, DAYS)))
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
        assertThat(listedVolunteers.size()).isEqualTo(2);

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
    void return_204_when_cancel_a_proposal_successfully() throws Exception {
        //GIVEN
        String proposalId = testData.registerESALAndProposalWithInscribedVolunteers().getId();
        testData.createCredential("revisor@huellapositiva.com", UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, "revisor@huellapositiva.com", DEFAULT_PASSWORD);

        //WHEN + THEN
        mvc.perform(post(FETCH_PROPOSAL_URI + proposalId + "/status/cancel")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(new ProposalCancelReasonDto(DEFAULT_CANCEL_REASON)))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andReturn().getResponse();

        JpaProposal jpaProposal = jpaProposalRepository.findByNaturalId(proposalId).orElseThrow(EntityNotFoundException::new);
        assertThat(jpaProposal.getStatus().getId()).isEqualTo(CANCELLED.getId());
        assertThat(jpaProposal.getCancelReason()).isEqualTo(DEFAULT_CANCEL_REASON);
    }

    @Test
    void return_409_when_cancel_a_finished_proposal() throws Exception {
        //GIVEN
        String proposalId = testData.registerESALAndProposal(FINISHED).getId();
        testData.createCredential("revisor@huellapositiva.com", UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, "revisor@huellapositiva.com", DEFAULT_PASSWORD);

        //WHEN + THEN
        mvc.perform(post(FETCH_PROPOSAL_URI + proposalId + "/status/cancel")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(new ProposalCancelReasonDto(DEFAULT_CANCEL_REASON)))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andReturn().getResponse();

        JpaProposal jpaProposal = jpaProposalRepository.findByNaturalId(proposalId).orElseThrow(EntityNotFoundException::new);
        assertThat(jpaProposal.getStatus().getId()).isEqualTo(FINISHED.getId());
    }

    @Test
    void return_409_when_cancel_an_inadequate_proposal() throws Exception {
        //GIVEN
        String proposalId = testData.registerESALAndProposal(INADEQUATE).getId();
        testData.createCredential("revisor@huellapositiva.com", UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, "revisor@huellapositiva.com", DEFAULT_PASSWORD);

        //WHEN + THEN
        mvc.perform(post(FETCH_PROPOSAL_URI + proposalId + "/status/cancel")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(new ProposalCancelReasonDto(DEFAULT_CANCEL_REASON)))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andReturn().getResponse();

        JpaProposal jpaProposal = jpaProposalRepository.findByNaturalId(proposalId).orElseThrow(EntityNotFoundException::new);
        assertThat(jpaProposal.getStatus().getId()).isEqualTo(INADEQUATE.getId());
    }

    @Test
    void return_409_when_cancel_an_already_cancelled_proposal() throws Exception {
        //GIVEN
        String proposalId = testData.registerESALAndProposal(CANCELLED).getId();
        testData.createCredential("revisor@huellapositiva.com", UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, "revisor@huellapositiva.com", DEFAULT_PASSWORD);

        //WHEN + THEN
        mvc.perform(post(FETCH_PROPOSAL_URI + proposalId + "/status/cancel")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(new ProposalCancelReasonDto(DEFAULT_CANCEL_REASON)))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andReturn().getResponse();

        JpaProposal jpaProposal = jpaProposalRepository.findByNaturalId(proposalId).orElseThrow(EntityNotFoundException::new);
        assertThat(jpaProposal.getStatus().getId()).isEqualTo(CANCELLED.getId());
    }

    @Test
    void return_404_when_trying_to_cancel_a_non_existing_proposal() throws Exception {
        //GIVEN
        String proposalId = "999";
        testData.createCredential("revisor@huellapositiva.com", UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, "revisor@huellapositiva.com", DEFAULT_PASSWORD);

        //WHEN + THEN
        mvc.perform(post(FETCH_PROPOSAL_URI + proposalId + "/status/cancel")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(new ProposalCancelReasonDto(DEFAULT_CANCEL_REASON)))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
    }

    @Test
    void return_204_when_change_status_rejected_or_confirmed() throws Exception {
        // GIVEN
        testData.registerESALAndProposalWithInscribedVolunteers();

        // WHEN
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);
        List<ChangeStatusVolunteerDto> changeStatusVolunteerDtos = new ArrayList<>();
        List<JpaVolunteerProposal> jpaVolunteerProposals = jpaVolunteersProposalsRepository.findAll();
        for(JpaVolunteerProposal volunteerProposal : jpaVolunteerProposals){
            if(changeStatusVolunteerDtos.isEmpty()) {
                changeStatusVolunteerDtos.add(new ChangeStatusVolunteerDto(volunteerProposal.getProposal(), volunteerProposal.getVolunteer().getId(), false));
            } else {
                changeStatusVolunteerDtos.add(new ChangeStatusVolunteerDto(volunteerProposal.getProposal(), volunteerProposal.getVolunteer().getId(), true));
            }
        }

        // THEN
        mvc.perform(post(FETCH_PROPOSAL_URI + "changeStatusVolunteerProposal")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(changeStatusVolunteerDtos))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        List<JpaVolunteerProposal> volunteersProposalsModified = jpaVolunteersProposalsRepository.findAll();
        assertThat(volunteersProposalsModified.get(0).isConfirmed()).isFalse();
        assertThat(volunteersProposalsModified.get(1).isConfirmed()).isTrue();
    }

    @Test
    void return_204_when_update_proposal_to_enrollment_closed_successfully() throws Exception {
        //GIVEN
        String proposalId = testData.registerESALAndProposal(PUBLISHED).getId();
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);

        //WHEN + THEN
        mvc.perform(put(format(FETCH_PROPOSAL_URI + "/%s/status/close", proposalId))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andReturn().getResponse();

        JpaProposal jpaProposal = jpaProposalRepository.findByNaturalId(proposalId).orElseThrow(EntityNotFoundException::new);
        assertThat(jpaProposal.getStatus().getId()).isEqualTo(ENROLLMENT_CLOSED.getId());
    }

    @ParameterizedTest
    @MethodSource("provideEnrollmentNotCloseableStatus")
    void return_409_when_proposal_status_is_not_published(ProposalStatus proposalStatus) throws Exception {
        //GIVEN
        JpaProposal jpaProposal = testData.registerESALAndProposal(proposalStatus);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);

        //WHEN + THEN
        mvc.perform(put(format(FETCH_PROPOSAL_URI + "/%s/status/close", jpaProposal.getId()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
    }

    private static Stream<ProposalStatus> provideEnrollmentNotCloseableStatus() {
        return Stream.of(
                CHANGES_REQUESTED,
                CANCELLED,
                INADEQUATE,
                FINISHED,
                ENROLLMENT_CLOSED,
                REVIEW_PENDING
        );
    }

    @ParameterizedTest
    @MethodSource("provideCorrectProposalInformation")
    void return_200_when_updates_proposal_and_change_status_to_review_pending(UpdateProposalRequestDto updateProposalRequestDto) throws Exception {
        // GIVEN
        JpaProposal jpaProposal = testData.registerESALAndProposal(REVIEW_PENDING);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);

        // WHEN + THEN
        mvc.perform(put(FETCH_PROPOSAL_URI + jpaProposal.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(updateProposalRequestDto))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        Proposal proposal = proposalRepository.fetch(jpaProposal.getId());
        assertThat(proposal.getStatus()).isEqualTo(REVIEW_PENDING);
        assertThat(proposal.getTitle()).isEqualTo(updateProposalRequestDto.getTitle());
        assertThat(proposal.getLocation().getProvince()).isEqualTo(updateProposalRequestDto.getProvince());
        assertThat(proposal.getLocation().getTown()).isEqualTo(updateProposalRequestDto.getTown());
        assertThat(proposal.getLocation().getAddress()).isEqualTo(updateProposalRequestDto.getAddress());
        assertThat(proposal.getLocation().getIsland()).isEqualTo(updateProposalRequestDto.getIsland());
        assertThat(proposal.getLocation().getZipCode()).isEqualTo(updateProposalRequestDto.getZipCode());
        assertThat(proposal.getRequiredDays()).isEqualTo(updateProposalRequestDto.getRequiredDays());
        assertThat(proposal.getPermittedAgeRange().getMinimum()).isEqualTo(updateProposalRequestDto.getMinimumAge());
        assertThat(proposal.getPermittedAgeRange().getMaximum()).isEqualTo(updateProposalRequestDto.getMaximumAge());
        if (updateProposalRequestDto.getStartingProposalDate() != null) {
            String proposalStartingDate = new ProposalDate(new SimpleDateFormat("yyyy-MM-dd").parse(updateProposalRequestDto.getStartingProposalDate().toString())).toString();
            String proposalStartingDateToCheck = proposal.getStartingProposalDate().toString();
            assertThat(proposalStartingDateToCheck).isEqualTo(proposalStartingDate);
        }

        String proposalClosingDate = new ProposalDate(new SimpleDateFormat("yyyy-MM-dd").parse(updateProposalRequestDto.getClosingProposalDate().toString())).toString();
        String proposalClosingDateToCheck = proposal.getClosingProposalDate().toString();
        assertThat(proposalClosingDateToCheck).isEqualTo(proposalClosingDate);

        String proposalStartingVolunteringDate = new ProposalDate(new SimpleDateFormat("yyyy-MM-dd").parse(updateProposalRequestDto.getStartingVolunteeringDate().toString())).toString();
        String proposalStartingVolunteringDateToCheck = proposal.getStartingVolunteeringDate().toString();
        assertThat(proposalStartingVolunteringDateToCheck).isEqualTo(proposalStartingVolunteringDate);

        assertThat(proposal.getDescription()).isEqualTo(updateProposalRequestDto.getDescription());
        assertThat(proposal.getDurationInDays()).isEqualTo(updateProposalRequestDto.getDurationInDays());

        String proposalCategory = proposal.getCategory().toString();
        assertThat(proposalCategory).isEqualTo(updateProposalRequestDto.getCategory());

        if (updateProposalRequestDto.getSkills() != null) {
            assertThat(proposal.getSkills().size()).isEqualTo(updateProposalRequestDto.getSkills().size());
        }
        if (updateProposalRequestDto.getRequirements() != null) {
            assertThat(proposal.getRequirements().size()).isEqualTo(updateProposalRequestDto.getRequirements().size());
        }
        if (updateProposalRequestDto.getExtraInfo() != null) {
            assertThat(proposal.getExtraInfo()).isEqualTo(updateProposalRequestDto.getExtraInfo());
        }
        if (updateProposalRequestDto.getInstructions() != null) {
            assertThat(proposal.getInstructions()).isEqualTo(updateProposalRequestDto.getInstructions());
        }
    }

    private static Stream<UpdateProposalRequestDto> provideCorrectProposalInformation() {
        return Stream.of(
                UpdateProposalRequestDto.builder()
                        .title(VALID_TITLE)
                        .province(VALID_PROVINCE)
                        .town(VALID_TOWN)
                        .address(VALID_ADDRESS)
                        .island(VALID_ISLAND)
                        .zipCode(VALID_ZIPCODE)
                        .requiredDays(VALID_REQUIRED_DAYS)
                        .minimumAge(VALID_MINIMUM_AGE)
                        .maximumAge(VALID_MAXIMUM_AGE)
                        .startingProposalDate(VALID_PROPOSAL_DATE)
                        .closingProposalDate(VALID_CLOSING_PROPOSAL_DATE)
                        .startingVolunteeringDate(VALID_STARTING_VOLUNTERING_DATE)
                        .description(VALID_DESCRIPTION)
                        .durationInDays(VALID_DURATION_IN_DAYS)
                        .category(VALID_CATEGORY)
                        .skills(VALID_SKILLS)
                        .requirements(VALID_REQUIREMENTS)
                        .extraInfo(VALID_EXTRA_INFO)
                        .instructions(VALID_INSTRUCTIONS)
                        .build(),
                UpdateProposalRequestDto.builder()
                        .title(VALID_TITLE)
                        .province(VALID_PROVINCE)
                        .town(VALID_TOWN)
                        .address(VALID_ADDRESS)
                        .island(VALID_ISLAND)
                        .zipCode(VALID_ZIPCODE)
                        .requiredDays(VALID_REQUIRED_DAYS)
                        .minimumAge(VALID_MINIMUM_AGE)
                        .maximumAge(VALID_MAXIMUM_AGE)
                        .closingProposalDate(VALID_CLOSING_PROPOSAL_DATE)
                        .startingVolunteeringDate(VALID_STARTING_VOLUNTERING_DATE)
                        .description(VALID_DESCRIPTION)
                        .durationInDays(VALID_DURATION_IN_DAYS)
                        .category(VALID_CATEGORY)
                        .build()
        );
    }

    @ParameterizedTest
    @MethodSource("provideIncorrectProposalInformation")
    void return_400_when_tries_update_proposal_with_wrong_fields(UpdateProposalRequestDto updateProposalRequestDto) throws Exception {
        // GIVEN
        JpaProposal jpaProposal = testData.registerESALAndProposal(REVIEW_PENDING);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);

        // WHEN + THEN
        mvc.perform(put(FETCH_PROPOSAL_URI + jpaProposal.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(updateProposalRequestDto))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    private static Stream<UpdateProposalRequestDto> provideIncorrectProposalInformation() {
        return Stream.of(
                UpdateProposalRequestDto.builder()
                        .title("Una propo//sal no va~$lida")
                        .province(VALID_PROVINCE)
                        .town(VALID_TOWN)
                        .island(VALID_ISLAND)
                        .zipCode(VALID_ZIPCODE)
                        .requiredDays(VALID_REQUIRED_DAYS)
                        .minimumAge(VALID_MINIMUM_AGE)
                        .maximumAge(VALID_MAXIMUM_AGE)
                        .closingProposalDate(VALID_CLOSING_PROPOSAL_DATE)
                        .startingVolunteeringDate(VALID_STARTING_VOLUNTERING_DATE)
                        .description(VALID_DESCRIPTION)
                        .durationInDays(VALID_DURATION_IN_DAYS)
                        .category(VALID_CATEGORY)
                        .build(),
                UpdateProposalRequestDto.builder()
                        .title("Titulo mayor a 75 caracteres,  Titulo mayor a 75 caracteres, Titulo mayor a 75 caracteres")
                        .province(VALID_PROVINCE)
                        .town(VALID_TOWN)
                        .island(VALID_ISLAND)
                        .zipCode(VALID_ZIPCODE)
                        .requiredDays(VALID_REQUIRED_DAYS)
                        .minimumAge(VALID_MINIMUM_AGE)
                        .maximumAge(VALID_MAXIMUM_AGE)
                        .closingProposalDate(VALID_CLOSING_PROPOSAL_DATE)
                        .startingVolunteeringDate(VALID_STARTING_VOLUNTERING_DATE)
                        .description(VALID_DESCRIPTION)
                        .durationInDays(VALID_DURATION_IN_DAYS)
                        .category(VALID_CATEGORY)
                        .build(),
                UpdateProposalRequestDto.builder()
                        .title(VALID_TITLE)
                        .province("Malaga")
                        .town(VALID_TOWN)
                        .island(VALID_ISLAND)
                        .zipCode(VALID_ZIPCODE)
                        .requiredDays(VALID_REQUIRED_DAYS)
                        .minimumAge(VALID_MINIMUM_AGE)
                        .maximumAge(VALID_MAXIMUM_AGE)
                        .closingProposalDate(VALID_CLOSING_PROPOSAL_DATE)
                        .startingVolunteeringDate(VALID_STARTING_VOLUNTERING_DATE)
                        .description(VALID_DESCRIPTION)
                        .durationInDays(VALID_DURATION_IN_DAYS)
                        .category(VALID_CATEGORY)
                        .build(),
                UpdateProposalRequestDto.builder()
                        .title(VALID_TITLE)
                        .province(VALID_PROVINCE)
                        .town("1215125")
                        .island(VALID_ISLAND)
                        .zipCode(VALID_ZIPCODE)
                        .requiredDays(VALID_REQUIRED_DAYS)
                        .minimumAge(VALID_MINIMUM_AGE)
                        .maximumAge(VALID_MAXIMUM_AGE)
                        .closingProposalDate(VALID_CLOSING_PROPOSAL_DATE)
                        .startingVolunteeringDate(VALID_STARTING_VOLUNTERING_DATE)
                        .description(VALID_DESCRIPTION)
                        .durationInDays(VALID_DURATION_IN_DAYS)
                        .category(VALID_CATEGORY)
                        .build(),
                UpdateProposalRequestDto.builder()
                        .title(VALID_TITLE)
                        .province(VALID_PROVINCE)
                        .town(VALID_TOWN)
                        .island("Mallorca")
                        .zipCode(VALID_ZIPCODE)
                        .requiredDays(VALID_REQUIRED_DAYS)
                        .minimumAge(VALID_MINIMUM_AGE)
                        .maximumAge(VALID_MAXIMUM_AGE)
                        .closingProposalDate(VALID_CLOSING_PROPOSAL_DATE)
                        .startingVolunteeringDate(VALID_STARTING_VOLUNTERING_DATE)
                        .description(VALID_DESCRIPTION)
                        .durationInDays(VALID_DURATION_IN_DAYS)
                        .category(VALID_CATEGORY)
                        .build(),
                UpdateProposalRequestDto.builder()
                        .title(VALID_TITLE)
                        .province(VALID_PROVINCE)
                        .town(VALID_TOWN)
                        .island(VALID_ISLAND)
                        .zipCode("40000")
                        .requiredDays(VALID_REQUIRED_DAYS)
                        .minimumAge(VALID_MINIMUM_AGE)
                        .maximumAge(VALID_MAXIMUM_AGE)
                        .closingProposalDate(VALID_CLOSING_PROPOSAL_DATE)
                        .startingVolunteeringDate(VALID_STARTING_VOLUNTERING_DATE)
                        .description(VALID_DESCRIPTION)
                        .durationInDays(VALID_DURATION_IN_DAYS)
                        .category(VALID_CATEGORY)
                        .build(),
                UpdateProposalRequestDto.builder()
                        .title(VALID_TITLE)
                        .province(VALID_PROVINCE)
                        .town(VALID_TOWN)
                        .island(VALID_ISLAND)
                        .zipCode(VALID_ZIPCODE)
                        .requiredDays("%&/()")
                        .minimumAge(VALID_MINIMUM_AGE)
                        .maximumAge(VALID_MAXIMUM_AGE)
                        .closingProposalDate(VALID_CLOSING_PROPOSAL_DATE)
                        .startingVolunteeringDate(VALID_STARTING_VOLUNTERING_DATE)
                        .description(VALID_DESCRIPTION)
                        .durationInDays(VALID_DURATION_IN_DAYS)
                        .category(VALID_CATEGORY)
                        .build(),
                UpdateProposalRequestDto.builder()
                        .title(VALID_TITLE)
                        .province(VALID_PROVINCE)
                        .town(VALID_TOWN)
                        .island(VALID_ISLAND)
                        .zipCode(VALID_ZIPCODE)
                        .requiredDays(VALID_REQUIRED_DAYS)
                        .minimumAge(15)
                        .maximumAge(VALID_MAXIMUM_AGE)
                        .closingProposalDate(VALID_CLOSING_PROPOSAL_DATE)
                        .startingVolunteeringDate(VALID_STARTING_VOLUNTERING_DATE)
                        .description(VALID_DESCRIPTION)
                        .durationInDays(VALID_DURATION_IN_DAYS)
                        .category(VALID_CATEGORY)
                        .build(),
                UpdateProposalRequestDto.builder()
                        .title(VALID_TITLE)
                        .province(VALID_PROVINCE)
                        .town(VALID_TOWN)
                        .island(VALID_ISLAND)
                        .zipCode(VALID_ZIPCODE)
                        .requiredDays(VALID_REQUIRED_DAYS)
                        .minimumAge(VALID_MINIMUM_AGE)
                        .maximumAge(90)
                        .closingProposalDate(VALID_CLOSING_PROPOSAL_DATE)
                        .startingVolunteeringDate(VALID_STARTING_VOLUNTERING_DATE)
                        .description(VALID_DESCRIPTION)
                        .durationInDays(VALID_DURATION_IN_DAYS)
                        .category(VALID_CATEGORY)
                        .build(),
                UpdateProposalRequestDto.builder()
                        .title(VALID_TITLE)
                        .province(VALID_PROVINCE)
                        .town(VALID_TOWN)
                        .island(VALID_ISLAND)
                        .zipCode(VALID_ZIPCODE)
                        .requiredDays(VALID_REQUIRED_DAYS)
                        .minimumAge(70)
                        .maximumAge(20)
                        .closingProposalDate(VALID_CLOSING_PROPOSAL_DATE)
                        .startingVolunteeringDate(VALID_STARTING_VOLUNTERING_DATE)
                        .description(VALID_DESCRIPTION)
                        .durationInDays(VALID_DURATION_IN_DAYS)
                        .category(VALID_CATEGORY)
                        .build(),
                UpdateProposalRequestDto.builder()
                        .title(VALID_TITLE)
                        .province(VALID_PROVINCE)
                        .town(VALID_TOWN)
                        .island(VALID_ISLAND)
                        .zipCode(VALID_ZIPCODE)
                        .requiredDays(VALID_REQUIRED_DAYS)
                        .minimumAge(VALID_MINIMUM_AGE)
                        .maximumAge(VALID_MAXIMUM_AGE)
                        .closingProposalDate(VALID_CLOSING_PROPOSAL_DATE)
                        .startingVolunteeringDate(LocalDate.now().minusDays(10))
                        .description(VALID_DESCRIPTION)
                        .durationInDays(VALID_DURATION_IN_DAYS)
                        .category(VALID_CATEGORY)
                        .build(),
                UpdateProposalRequestDto.builder()
                        .title(VALID_TITLE)
                        .province(VALID_PROVINCE)
                        .town(VALID_TOWN)
                        .island(VALID_ISLAND)
                        .zipCode(VALID_ZIPCODE)
                        .requiredDays(VALID_REQUIRED_DAYS)
                        .minimumAge(VALID_MINIMUM_AGE)
                        .maximumAge(VALID_MAXIMUM_AGE)
                        .startingProposalDate(VALID_PROPOSAL_DATE)
                        .closingProposalDate(LocalDate.now().minusDays(10))
                        .startingVolunteeringDate(VALID_STARTING_VOLUNTERING_DATE)
                        .description(VALID_DESCRIPTION)
                        .durationInDays(VALID_DURATION_IN_DAYS)
                        .category(VALID_CATEGORY)
                        .build(),
                UpdateProposalRequestDto.builder()
                        .title(VALID_TITLE)
                        .province(VALID_PROVINCE)
                        .town(VALID_TOWN)
                        .island(VALID_ISLAND)
                        .zipCode(VALID_ZIPCODE)
                        .requiredDays(VALID_REQUIRED_DAYS)
                        .minimumAge(VALID_MINIMUM_AGE)
                        .maximumAge(VALID_MAXIMUM_AGE)
                        .startingProposalDate(VALID_PROPOSAL_DATE)
                        .closingProposalDate(VALID_CLOSING_PROPOSAL_DATE)
                        .startingVolunteeringDate(LocalDate.now().plusDays(7))
                        .description(VALID_DESCRIPTION)
                        .durationInDays(VALID_DURATION_IN_DAYS)
                        .category(VALID_CATEGORY)
                        .build(),
                UpdateProposalRequestDto.builder()
                        .title(VALID_TITLE)
                        .province(VALID_PROVINCE)
                        .town(VALID_TOWN)
                        .island(VALID_ISLAND)
                        .zipCode(VALID_ZIPCODE)
                        .requiredDays(VALID_REQUIRED_DAYS)
                        .minimumAge(VALID_MINIMUM_AGE)
                        .maximumAge(VALID_MAXIMUM_AGE)
                        .startingProposalDate(VALID_PROPOSAL_DATE)
                        .closingProposalDate(VALID_CLOSING_PROPOSAL_DATE)
                        .startingVolunteeringDate(VALID_STARTING_VOLUNTERING_DATE)
                        .description("")
                        .durationInDays(VALID_DURATION_IN_DAYS)
                        .category(VALID_CATEGORY)
                        .build(),
                UpdateProposalRequestDto.builder()
                        .title(VALID_TITLE)
                        .province(VALID_PROVINCE)
                        .town(VALID_TOWN)
                        .island(VALID_ISLAND)
                        .zipCode(VALID_ZIPCODE)
                        .requiredDays(VALID_REQUIRED_DAYS)
                        .minimumAge(VALID_MINIMUM_AGE)
                        .maximumAge(VALID_MAXIMUM_AGE)
                        .startingProposalDate(VALID_PROPOSAL_DATE)
                        .closingProposalDate(VALID_CLOSING_PROPOSAL_DATE)
                        .startingVolunteeringDate(VALID_STARTING_VOLUNTERING_DATE)
                        .description("a".repeat(201))
                        .durationInDays(VALID_DURATION_IN_DAYS)
                        .category(VALID_CATEGORY)
                        .build(),
                UpdateProposalRequestDto.builder()
                        .title(VALID_TITLE)
                        .province(VALID_PROVINCE)
                        .town(VALID_TOWN)
                        .island(VALID_ISLAND)
                        .zipCode(VALID_ZIPCODE)
                        .requiredDays(VALID_REQUIRED_DAYS)
                        .minimumAge(VALID_MINIMUM_AGE)
                        .maximumAge(VALID_MAXIMUM_AGE)
                        .startingProposalDate(VALID_PROPOSAL_DATE)
                        .closingProposalDate(VALID_CLOSING_PROPOSAL_DATE)
                        .startingVolunteeringDate(VALID_STARTING_VOLUNTERING_DATE)
                        .description(VALID_DESCRIPTION)
                        .durationInDays("Campo de letras")
                        .category(VALID_CATEGORY)
                        .build(),
                UpdateProposalRequestDto.builder()
                        .title(VALID_TITLE)
                        .province(VALID_PROVINCE)
                        .town(VALID_TOWN)
                        .island(VALID_ISLAND)
                        .zipCode(VALID_ZIPCODE)
                        .requiredDays(VALID_REQUIRED_DAYS)
                        .minimumAge(VALID_MINIMUM_AGE)
                        .maximumAge(VALID_MAXIMUM_AGE)
                        .startingProposalDate(VALID_PROPOSAL_DATE)
                        .closingProposalDate(VALID_CLOSING_PROPOSAL_DATE)
                        .startingVolunteeringDate(VALID_STARTING_VOLUNTERING_DATE)
                        .description(VALID_DESCRIPTION)
                        .durationInDays(VALID_DURATION_IN_DAYS)
                        .category("Categoria no valida")
                        .build(),
                UpdateProposalRequestDto.builder()
                        .title(VALID_TITLE)
                        .province(VALID_PROVINCE)
                        .town(VALID_TOWN)
                        .island(VALID_ISLAND)
                        .zipCode(VALID_ZIPCODE)
                        .requiredDays(VALID_REQUIRED_DAYS)
                        .minimumAge(VALID_MINIMUM_AGE)
                        .maximumAge(VALID_MAXIMUM_AGE)
                        .startingProposalDate(VALID_PROPOSAL_DATE)
                        .closingProposalDate(VALID_CLOSING_PROPOSAL_DATE)
                        .startingVolunteeringDate(VALID_STARTING_VOLUNTERING_DATE)
                        .description(VALID_DESCRIPTION)
                        .durationInDays(VALID_DURATION_IN_DAYS)
                        .category(VALID_CATEGORY)
                        .extraInfo("a".repeat(201))
                        .build(),
                UpdateProposalRequestDto.builder()
                        .title(VALID_TITLE)
                        .province(VALID_PROVINCE)
                        .town(VALID_TOWN)
                        .island(VALID_ISLAND)
                        .zipCode(VALID_ZIPCODE)
                        .requiredDays(VALID_REQUIRED_DAYS)
                        .minimumAge(VALID_MINIMUM_AGE)
                        .maximumAge(VALID_MAXIMUM_AGE)
                        .startingProposalDate(VALID_PROPOSAL_DATE)
                        .closingProposalDate(VALID_CLOSING_PROPOSAL_DATE)
                        .startingVolunteeringDate(VALID_STARTING_VOLUNTERING_DATE)
                        .description(VALID_DESCRIPTION)
                        .durationInDays(VALID_DURATION_IN_DAYS)
                        .category(VALID_CATEGORY)
                        .instructions("a".repeat(201))
                        .build()
        );
    }

    @Test
    void return_404_when_tries_update_proposal_with_wrong_proposal_id() throws Exception {
        // GIVEN
        testData.registerESALAndProposal(REVIEW_PENDING);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);

        // WHEN
        UpdateProposalRequestDto updateProposalRequestDto = UpdateProposalRequestDto.builder()
                .title(VALID_TITLE)
                .province(VALID_PROVINCE)
                .town(VALID_TOWN)
                .address(VALID_ADDRESS)
                .island(VALID_ISLAND)
                .zipCode(VALID_ZIPCODE)
                .requiredDays(VALID_REQUIRED_DAYS)
                .minimumAge(VALID_MINIMUM_AGE)
                .maximumAge(VALID_MAXIMUM_AGE)
                .closingProposalDate(VALID_CLOSING_PROPOSAL_DATE)
                .startingVolunteeringDate(VALID_STARTING_VOLUNTERING_DATE)
                .description(VALID_DESCRIPTION)
                .durationInDays(VALID_DURATION_IN_DAYS)
                .category(VALID_CATEGORY)
                .build();

        // THEN
        mvc.perform(put(FETCH_PROPOSAL_URI + "abcd1234-abcd-1234-abcd-abcdef123456")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(updateProposalRequestDto))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void return_403_when_tries_update_proposal_with_wrong_contact_person() throws Exception {
        // GIVEN
        JpaProposal jpaProposal = testData.registerESALAndProposal(REVIEW_PENDING);
        testData.createESALJpaContactPerson(VALID_NAME, VALID_SURNAME, VALID_PHONE, "contactPersonHacker@huellapositiva.com", DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, "contactPersonHacker@huellapositiva.com", DEFAULT_PASSWORD);

        // WHEN
        UpdateProposalRequestDto updateProposalRequestDto = UpdateProposalRequestDto.builder()
                .title(VALID_TITLE)
                .province(VALID_PROVINCE)
                .town(VALID_TOWN)
                .address(VALID_ADDRESS)
                .island(VALID_ISLAND)
                .zipCode(VALID_ZIPCODE)
                .requiredDays(VALID_REQUIRED_DAYS)
                .minimumAge(VALID_MINIMUM_AGE)
                .maximumAge(VALID_MAXIMUM_AGE)
                .closingProposalDate(VALID_CLOSING_PROPOSAL_DATE)
                .startingVolunteeringDate(VALID_STARTING_VOLUNTERING_DATE)
                .description(VALID_DESCRIPTION)
                .durationInDays(VALID_DURATION_IN_DAYS)
                .category(VALID_CATEGORY)
                .build();

        // THEN
        mvc.perform(put(FETCH_PROPOSAL_URI + jpaProposal.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(updateProposalRequestDto))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @MethodSource("provideProposalWithDuplicatedSkillOrRequirements")
    void should_return_204_ignoring_duplicated_skills_or_requirements(UpdateProposalRequestDto updateProposalRequestDto) throws Exception {
        // GIVEN
        JpaProposal jpaProposal = testData.registerESALAndProposal(REVIEW_PENDING);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);

        // WHEN + THEN
        mvc.perform(put(FETCH_PROPOSAL_URI + jpaProposal.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(updateProposalRequestDto))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    private static Stream<UpdateProposalRequestDto> provideProposalWithDuplicatedSkillOrRequirements() {
        return Stream.of(
                UpdateProposalRequestDto.builder()
                        .title(VALID_TITLE)
                        .province(VALID_PROVINCE)
                        .town(VALID_TOWN)
                        .address(VALID_ADDRESS)
                        .island(VALID_ISLAND)
                        .zipCode(VALID_ZIPCODE)
                        .requiredDays(VALID_REQUIRED_DAYS)
                        .minimumAge(VALID_MINIMUM_AGE)
                        .maximumAge(VALID_MAXIMUM_AGE)
                        .closingProposalDate(VALID_CLOSING_PROPOSAL_DATE)
                        .startingVolunteeringDate(VALID_STARTING_VOLUNTERING_DATE)
                        .description(VALID_DESCRIPTION)
                        .durationInDays(VALID_DURATION_IN_DAYS)
                        .category(VALID_CATEGORY)
                        .skills(List.of(
                                new SkillDto("SkillRepetida", "descipcion"),
                                new SkillDto("SkillRepetida", "descipcion")))
                        .build(),
                UpdateProposalRequestDto.builder()
                        .title(VALID_TITLE)
                        .province(VALID_PROVINCE)
                        .town(VALID_TOWN)
                        .address(VALID_ADDRESS)
                        .island(VALID_ISLAND)
                        .zipCode(VALID_ZIPCODE)
                        .requiredDays(VALID_REQUIRED_DAYS)
                        .minimumAge(VALID_MINIMUM_AGE)
                        .maximumAge(VALID_MAXIMUM_AGE)
                        .closingProposalDate(VALID_CLOSING_PROPOSAL_DATE)
                        .startingVolunteeringDate(VALID_STARTING_VOLUNTERING_DATE)
                        .description(VALID_DESCRIPTION)
                        .durationInDays(VALID_DURATION_IN_DAYS)
                        .category(VALID_CATEGORY)
                        .requirements(List.of(
                                "Un requerimiento",
                                "Un requerimiento"))
                        .build()
        );
    }

    @ParameterizedTest
    @MethodSource("providePublishableStatus")
    void return_204_when_update_proposal_to_published_successfully(ProposalStatus proposalStatus) throws Exception {
        //GIVEN
        testData.createCredential(DEFAULT_EMAIL_REVISER, UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        String proposalId = testData.registerESALAndProposal(proposalStatus).getId();
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL_REVISER, DEFAULT_PASSWORD);

        //WHEN + THEN
        mvc.perform(put(format(FETCH_PROPOSAL_URI + "/%s/status/publish", proposalId))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andReturn().getResponse();

        JpaProposal jpaProposal = jpaProposalRepository.findByNaturalId(proposalId).orElseThrow(EntityNotFoundException::new);
        assertThat(jpaProposal.getStatus().getId()).isEqualTo(PUBLISHED.getId());
    }

    private static Stream<ProposalStatus> providePublishableStatus() {
        return Stream.of(
                REVIEW_PENDING,
                ENROLLMENT_CLOSED
        );
    }

    @ParameterizedTest
    @MethodSource("provideNotPublishableStatus")
    void return_409_when_proposal_status_is_no_review_pending_or_enrollment_closed(ProposalStatus proposalStatus) throws Exception {
        //GIVEN
        testData.createCredential(DEFAULT_EMAIL_REVISER, UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JpaProposal jpaProposal = testData.registerESALAndProposal(proposalStatus);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL_REVISER, DEFAULT_PASSWORD);

        //WHEN + THEN
        mvc.perform(put(format(FETCH_PROPOSAL_URI + "/%s/status/publish", jpaProposal.getId()))

                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
    }

    private static Stream<ProposalStatus> provideNotPublishableStatus() {
        return Stream.of(
                CHANGES_REQUESTED,
                CANCELLED,
                INADEQUATE,
                FINISHED
        );
    }

    @Test
    void return_204_when_update_proposal_to_published_successfully_from_enrollment_closed() throws Exception {
        //GIVEN
        String proposalId = testData.registerESALAndProposal(ENROLLMENT_CLOSED).getId();
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);

        //WHEN + THEN
        mvc.perform(put(format(FETCH_PROPOSAL_URI + "/%s/status/publish", proposalId))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andReturn().getResponse();

        JpaProposal jpaProposal = jpaProposalRepository.findByNaturalId(proposalId).orElseThrow(EntityNotFoundException::new);
        assertThat(jpaProposal.getStatus().getId()).isEqualTo(PUBLISHED.getId());
    }

    @ParameterizedTest
    @MethodSource("provideNotPublishableFromEnrollmentClosedStatus")
    void return_409_when_proposal_status_is_not_enrollment_closed(ProposalStatus proposalStatus) throws Exception {
        //GIVEN
        JpaProposal jpaProposal = testData.registerESALAndProposal(proposalStatus);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);

        //WHEN + THEN
        mvc.perform(put(format(FETCH_PROPOSAL_URI + "/%s/status/publish", jpaProposal.getId()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andReturn().getResponse();
    }

    private static Stream<ProposalStatus> provideNotPublishableFromEnrollmentClosedStatus() {
        return Stream.of(
                CHANGES_REQUESTED,
                CANCELLED,
                INADEQUATE,
                FINISHED,
                PUBLISHED,
                REVIEW_PENDING
        );
    }

    @Test
    void return_204_when_proposal_image_changed_successfully() throws Exception {
        JpaProposal jpaProposal = testData.registerESALAndProposal(PUBLISHED);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);

        InputStream is = getClass().getClassLoader().getResourceAsStream("images/huellapositiva-logo.png");
        MockMultipartHttpServletRequestBuilder multipart = multipart(FETCH_PROPOSAL_URI + jpaProposal.getId() + "/image");
        multipart.with(request -> {
            request.setMethod("PUT");
            return request;
        });
        mvc.perform(multipart
                .file(new MockMultipartFile("photo", "photo-test.PNG", "image/png", is))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MULTIPART_FORM_DATA)
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        JpaProposal jpaProposal1 = jpaProposalRepository.findByNaturalId(jpaProposal.getId()).orElseThrow(EntityNotFoundException::new);
        assertThat(jpaProposal1.getImageUrl()).isNotNull();
    }

    @Test
    void return_400_when_proposal_status_is_different_from_published_or_review_pending() throws Exception {
        JpaProposal jpaProposal = testData.registerESALAndProposal(INADEQUATE);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);

        InputStream is = getClass().getClassLoader().getResourceAsStream("images/huellapositiva-logo.png");
        MockMultipartHttpServletRequestBuilder multipart = multipart(FETCH_PROPOSAL_URI + jpaProposal.getId() + "/image");
        multipart.with(request -> {
            request.setMethod("PUT");
            return request;
        });

        mvc.perform(multipart
                .file(new MockMultipartFile("photo", "photo-test.PNG", "image/png", is))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MULTIPART_FORM_DATA)
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @MethodSource("provideInvalidImages")
    void return_400_when_the_proposal_image_uploaded_is_too_big(List<String> proposalImageURI) throws Exception {
        JpaProposal jpaProposal = testData.registerESALAndProposal(PUBLISHED);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);

        MockMultipartFile file = new MockMultipartFile("proposalImage", proposalImageURI.get(0),
                proposalImageURI.get(1), getClass().getClassLoader().getResourceAsStream(proposalImageURI.get(0)));

        MockMultipartHttpServletRequestBuilder multipart = multipart(FETCH_PROPOSAL_URI + jpaProposal.getId() + "/image");
        multipart.with(request -> {
            request.setMethod("PUT");
            return request;
        });
        mvc.perform(multipart
                .file(file)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MULTIPART_FORM_DATA)
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    private static Stream<List<String>> provideInvalidImages(){
        return Stream.of(
                List.of("images/Sample-png-image-3mb.png","image/png"),
                List.of("images/oversized.png","image/png"),
                List.of("documents/pdf-test.pdf","application/pdf")
        );
    }

    @Test
    void return_400_when_there_is_not_photo_uploaded() throws Exception {
        JpaProposal jpaProposal = testData.registerESALAndProposal(PUBLISHED);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);

        MockMultipartHttpServletRequestBuilder multipart = multipart(FETCH_PROPOSAL_URI + jpaProposal.getId() + "/image");
        multipart.with(request -> {
            request.setMethod("PUT");
            return request;
        });
        mvc.perform(multipart
                .file(new MockMultipartFile("proposalImage", "photo-test.PNG", "image/png", InputStream.nullInputStream()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MULTIPART_FORM_DATA)
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void return_403_when_contact_person_email_is_not_equal_to_proposal_contact_person_email() throws Exception {
        JpaProposal jpaProposal = testData.registerESALAndProposal(PUBLISHED);
        testData.createESALJpaContactPerson(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        InputStream is = getClass().getClassLoader().getResourceAsStream("images/huellapositiva-logo.png");
        MockMultipartHttpServletRequestBuilder multipart = multipart(FETCH_PROPOSAL_URI + jpaProposal.getId() + "/image");
        multipart.with(request -> {
            request.setMethod("PUT");
            return request;
        });
        mvc.perform(multipart
                .file(new MockMultipartFile("photo", "photo-test.PNG", "image/png", is))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MULTIPART_FORM_DATA)
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void return_204_when_proposal_status_changed_to_inadequate() throws Exception {
        testData.createCredential("revisor@huellapositiva.com", UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, "revisor@huellapositiva.com", DEFAULT_PASSWORD);
        JpaProposal jpaProposal = testData.registerESALAndProposal(REVIEW_PENDING);
        ChangeToInadequateDto dto = new ChangeToInadequateDto("Pandemia");

        mvc.perform(put(format(FETCH_PROPOSAL_URI + "%s/status/inadequate", jpaProposal.getId()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(dto))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        JpaProposal proposal = jpaProposalRepository.findByNaturalId(jpaProposal.getId()).orElseThrow(EntityNotFoundException::new);
        assertThat(proposal.getStatus().getId()).isEqualTo(INADEQUATE.getId());
    }

    @Test
    void return_409_when_changing_to_inadequate_a_proposal_not_in_review_pending() throws Exception {
        testData.createCredential("revisor@huellapositiva.com", UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, "revisor@huellapositiva.com", DEFAULT_PASSWORD);
        JpaProposal jpaProposal = testData.registerESALAndProposal(PUBLISHED);
        ChangeToInadequateDto dto = new ChangeToInadequateDto("Pandemia");

        mvc.perform(put(format(FETCH_PROPOSAL_URI + "%s/status/inadequate", jpaProposal.getId()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(dto))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());

        JpaProposal proposal = jpaProposalRepository.findByNaturalId(jpaProposal.getId()).orElseThrow(EntityNotFoundException::new);
        assertThat(proposal.getStatus().getId()).isEqualTo(PUBLISHED.getId());
    }

    @Test
    void return_404_when_changing_to_inadequate_a_non_existing_proposal() throws Exception {
        testData.createCredential("revisor@huellapositiva.com", UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, "revisor@huellapositiva.com", DEFAULT_PASSWORD);
        ChangeToInadequateDto dto = new ChangeToInadequateDto("Pandemia");

        mvc.perform(put(format(FETCH_PROPOSAL_URI + "/%s/status/inadequate", 999))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(dto))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

    }
}