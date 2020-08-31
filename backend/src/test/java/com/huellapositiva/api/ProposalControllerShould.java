package com.huellapositiva.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.application.dto.ProposalRequestDto;
import com.huellapositiva.application.dto.ProposalResponseDto;
import com.huellapositiva.domain.model.valueobjects.ProposalCategory;
import com.huellapositiva.domain.model.valueobjects.Roles;
import com.huellapositiva.infrastructure.orm.entities.JpaESAL;
import com.huellapositiva.infrastructure.orm.entities.JpaContactPerson;
import com.huellapositiva.infrastructure.orm.entities.JpaProposal;
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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
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
        JpaContactPerson contactPerson = testData.createESALMember(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkESAL(contactPerson, JpaESAL.builder().id(UUID.randomUUID().toString()).name("Huella Positiva").build());
        ProposalRequestDto proposalDto = testData.buildUnpublishedProposalDto();
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN
        MockHttpServletResponse response = mvc.perform(MockMvcRequestBuilders.multipart(REGISTER_PROPOSAL_URI)
                .file(new MockMultipartFile("file","fileName", "text/plain", "test data".getBytes()))
                .file(new MockMultipartFile("dto","dto", "application/json", objectMapper.writeValueAsString(proposalDto).getBytes()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, matchesPattern("\\S+(/api/v1/proposals/)" + UUID_REGEX)))
                .andReturn().getResponse();

        // THEN
        String location = response.getHeader(HttpHeaders.LOCATION);
        String id = location.substring(location.lastIndexOf('/') + 1);
        assertThat(jpaProposalRepository.findByNaturalId(id).get().getTitle()).isEqualTo("Recogida de ropita");
    }

    @Test
    void return_400_when_date_is_invalid() throws Exception {
        // GIVEN
        JpaContactPerson contactPerson = testData.createESALMember(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkESAL(contactPerson, JpaESAL.builder().id(UUID.randomUUID().toString()).name("Huella Positiva").build());
        String invalidStartingDate = "20-08-2020";
        ProposalRequestDto proposalDto =  ProposalRequestDto.builder()
                .title("Recogida de ropita")
                .province("Santa Cruz de Tenerife")
                .town("Santa Cruz de Tenerife")
                .address("Avenida Weyler 4")
                .expirationDate("24-08-2020")
                .requiredDays("Weekends")
                .minimumAge(18)
                .maximumAge(26)
                .description("Recogida de ropa en la laguna")
                .durationInDays("1 semana")
                .startingDate(invalidStartingDate)
                .category(ProposalCategory.ON_SITE.toString())
                .skills(new String[][]{{"Habilidad", "Descripción"}, {"Negociación", "Saber regatear"}})
                .requirements(new String[]{"Forma física para cargar con la ropa", "Disponibilidad horaria", "Carnet de conducir"})
                .extraInfo("Es recomendable tener ganas de recoger ropa")
                .instructions("Se seleccionarán a los primeros 100 voluntarios")
                .build();
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN + THEN
        mvc.perform(post(REGISTER_PROPOSAL_URI)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(proposalDto))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void return_400_when_age_is_out_of_range() throws Exception {
        // GIVEN
        JpaContactPerson contactPerson = testData.createESALMember(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkESAL(contactPerson, JpaESAL.builder().id(UUID.randomUUID().toString()).name("Huella Positiva").build());
        int invalidMinimumAge = 17;
        ProposalRequestDto proposalDto =  ProposalRequestDto.builder()
                .title("Recogida de ropita")
                .province("Santa Cruz de Tenerife")
                .town("Santa Cruz de Tenerife")
                .address("Avenida Weyler 4")
                .expirationDate("24-08-2020")
                .requiredDays("Weekends")
                .minimumAge(invalidMinimumAge)
                .maximumAge(26)
                .description("Recogida de ropa en la laguna")
                .durationInDays("1 semana")
                .startingDate("25-08-2020")
                .category(ProposalCategory.ON_SITE.toString())
                .skills(new String[][]{{"Habilidad", "Descripción"}, {"Negociación", "Saber regatear"}})
                .requirements(new String[]{"Forma física para cargar con la ropa", "Disponibilidad horaria", "Carnet de conducir"})
                .extraInfo("Es recomendable tener ganas de recoger ropa")
                .instructions("Se seleccionarán a los primeros 100 voluntarios")
                .build();
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN + THEN
        mvc.perform(MockMvcRequestBuilders.multipart(REGISTER_PROPOSAL_URI)
                .file(new MockMultipartFile("user-file","fileName", "text/plain", "test data".getBytes()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(proposalDto))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void return_400_when_minimum_age_is_greater_than_maximum_age() throws Exception {
        // GIVEN
        JpaContactPerson contactPerson = testData.createESALMember(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkESAL(contactPerson, JpaESAL.builder().id(UUID.randomUUID().toString()).name("Huella Positiva").build());
        int invalidMinimumAge = 30;
        ProposalRequestDto proposalDto =  ProposalRequestDto.builder()
                .title("Recogida de ropita")
                .province("Santa Cruz de Tenerife")
                .town("Santa Cruz de Tenerife")
                .address("Avenida Weyler 4")
                .expirationDate("24-08-2020")
                .requiredDays("Weekends")
                .minimumAge(invalidMinimumAge)
                .maximumAge(26)
                .description("Recogida de ropa en la laguna")
                .durationInDays("1 semana")
                .startingDate("25-08-2020")
                .category(ProposalCategory.ON_SITE.toString())
                .skills(new String[][]{{"Habilidad", "Descripción"}, {"Negociación", "Saber regatear"}})
                .requirements(new String[]{"Forma física para cargar con la ropa", "Disponibilidad horaria", "Carnet de conducir"})
                .extraInfo("Es recomendable tener ganas de recoger ropa")
                .instructions("Se seleccionarán a los primeros 100 voluntarios")
                .build();
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN + THEN
        mvc.perform(post(REGISTER_PROPOSAL_URI)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(proposalDto))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void fetch_and_return_proposal() throws Exception {
        // GIVEN
        JpaProposal proposal = testData.registerESALAndPublishedProposal();

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
        JpaProposal proposal = testData.registerESALAndNotPublishedProposal();

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
        String proposalId = testData.registerESALandPublishedProposalObject();
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN
        mvc.perform(post("/api/v1/proposals/" + proposalId + "/join")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(APPLICATION_JSON)
                .with(csrf())
                .accept(APPLICATION_JSON))
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
    void return_410_when_joining_a_non_existent_proposal() throws Exception {
        // GIVEN
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JpaProposal proposal = testData.registerESALAndPublishedProposal();
        proposal.setExpirationDate(Date.from(Instant.now().minus(1, ChronoUnit.DAYS)));
        jpaProposalRepository.save(proposal);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN
        mvc.perform(post("/api/v1/proposals/" + proposal.getId() + "/join")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(APPLICATION_JSON)
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isGone());
    }

    @Test
    void create_proposal_as_reviser() throws Exception {
        testData.createCredential(DEFAULT_EMAIL, UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        JpaContactPerson contactPerson = testData.createESALMember(DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkESAL(contactPerson, JpaESAL.builder().id(UUID.randomUUID().toString()).name("Huella Positiva").build());
        ProposalRequestDto proposalRequestDto = testData.buildProposalDto(true);
        proposalRequestDto.setEsalName("Huella Positiva");

        mvc.perform(post("/api/v1/proposals/reviser")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(proposalRequestDto))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(header().string(HttpHeaders.LOCATION, matchesPattern("\\S+(/api/v1/proposals/)" + UUID_REGEX)))
                .andExpect(status().isCreated());

        assertThat(jpaProposalRepository.findAll()).isNotEmpty();
    }
}
