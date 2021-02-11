package com.huellapositiva.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.RegisterESALMemberRequestDto;
import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.application.dto.UpdateProfileRequestDto;
import com.huellapositiva.application.exception.EmailNotFoundException;
import com.huellapositiva.domain.model.valueobjects.Roles;
import com.huellapositiva.infrastructure.orm.entities.JpaContactPerson;
import com.huellapositiva.infrastructure.orm.repository.JpaContactPersonRepository;
import com.huellapositiva.infrastructure.security.JwtService;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Stream;

import static com.huellapositiva.util.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestData.class)
class ESALMemberControllerShould {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private TestData testData;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JpaContactPersonRepository jpaContactPersonRepository;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    void register_member_and_return_201_and_tokens() throws Exception {
        // GIVEN
        RegisterESALMemberRequestDto dto = new RegisterESALMemberRequestDto(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN
        MockHttpServletResponse response = mvc.perform(post("/api/v1/contactperson")
                .content(objectMapper.writeValueAsString(dto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, matchesPattern("\\S+(/api/v1/contactperson/)" + UUID_REGEX)))
                .andReturn()
                .getResponse();

        // THEN
        String jsonResponse = response.getContentAsString();
        JwtResponseDto responseDto = objectMapper.readValue(jsonResponse, JwtResponseDto.class);
        Pair<String, List<String>> userDetails = jwtService.getUserDetails(responseDto.getAccessToken());
        assertThat(userDetails.getFirst()).isEqualTo(dto.getEmail());
        assertThat(userDetails.getSecond()).hasSize(1);
        assertThat(userDetails.getSecond().get(0)).isEqualTo(Roles.CONTACT_PERSON_NOT_CONFIRMED.toString());
        String location = response.getHeader(HttpHeaders.LOCATION);
        String id = location.substring(location.lastIndexOf('/') + 1);
        assertThat(jpaContactPersonRepository.findByUUID(id).get().getCredential().getEmail()).isEqualTo(DEFAULT_EMAIL);

        JpaContactPerson jpaContactPerson = jpaContactPersonRepository.findByEmail(DEFAULT_EMAIL)
                .orElseThrow(() -> new EmailNotFoundException("Not found the email: " + DEFAULT_EMAIL));
        assertThat(jpaContactPerson.getName()).isEqualTo(VALID_NAME);
        assertThat(jpaContactPerson.getSurname()).isEqualTo(VALID_SURNAME);
        assertThat(jpaContactPerson.getPhoneNumber()).isEqualTo(VALID_PHONE);
    }

    @ParameterizedTest
    @MethodSource("provideIncorrectProfileInformation")
    void return_400_when_register_contact_person_with_malformed_data_request(RegisterESALMemberRequestDto registerESALMemberRequestDto) throws Exception {
        // GIVEN
        RegisterESALMemberRequestDto dto = registerESALMemberRequestDto;
        // WHEN
        mvc.perform(post("/api/v1/contactperson")
                .content(objectMapper.writeValueAsString(dto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    private static Stream<RegisterESALMemberRequestDto> provideIncorrectProfileInformation() {
        return Stream.of(
                RegisterESALMemberRequestDto.builder()
                        .name("ABCD2345")
                        .surname(VALID_SURNAME)
                        .phoneNumber(VALID_PHONE)
                        .email(DEFAULT_EMAIL)
                        .password(DEFAULT_PASSWORD)
                        .build(),
                RegisterESALMemberRequestDto.builder()
                        .name("")
                        .surname(VALID_SURNAME)
                        .phoneNumber(VALID_PHONE)
                        .email(DEFAULT_EMAIL)
                        .password(DEFAULT_PASSWORD)
                        .build(),
                RegisterESALMemberRequestDto.builder()
                        .name(VALID_NAME)
                        .surname("abcd1234")
                        .phoneNumber(VALID_PHONE)
                        .email(DEFAULT_EMAIL)
                        .password(DEFAULT_PASSWORD)
                        .build(),
                RegisterESALMemberRequestDto.builder()
                        .name(VALID_NAME)
                        .surname("")
                        .phoneNumber(VALID_PHONE)
                        .email(DEFAULT_EMAIL)
                        .password(DEFAULT_PASSWORD)
                        .build(),
                RegisterESALMemberRequestDto.builder()
                        .name(VALID_NAME)
                        .surname(VALID_SURNAME)
                        .phoneNumber("123987231589")
                        .email(DEFAULT_EMAIL)
                        .password(DEFAULT_PASSWORD)
                        .build(),
                RegisterESALMemberRequestDto.builder()
                        .name(VALID_NAME)
                        .surname(VALID_SURNAME)
                        .phoneNumber("")
                        .email(DEFAULT_EMAIL)
                        .password(DEFAULT_PASSWORD)
                        .build(),
                RegisterESALMemberRequestDto.builder()
                        .name(VALID_NAME)
                        .surname(VALID_SURNAME)
                        .phoneNumber(VALID_PHONE)
                        .email("foo@@@huellapositiva.com")
                        .password(DEFAULT_PASSWORD)
                        .build(),
                RegisterESALMemberRequestDto.builder()
                        .name(VALID_NAME)
                        .surname(VALID_SURNAME)
                        .phoneNumber(VALID_PHONE)
                        .email("   ")
                        .password(DEFAULT_PASSWORD)
                        .build(),
                RegisterESALMemberRequestDto.builder()
                        .name(VALID_NAME)
                        .surname(VALID_SURNAME)
                        .phoneNumber(VALID_PHONE)
                        .email(DEFAULT_EMAIL)
                        .password("asfas`´^][{}~~")
                        .build(),
                RegisterESALMemberRequestDto.builder()
                        .name(VALID_NAME)
                        .surname(VALID_SURNAME)
                        .phoneNumber(VALID_PHONE)
                        .email(DEFAULT_EMAIL)
                        .password("asfa")
                        .build(),
                RegisterESALMemberRequestDto.builder()
                        .name(VALID_NAME)
                        .surname(VALID_SURNAME)
                        .phoneNumber(VALID_PHONE)
                        .email(DEFAULT_EMAIL)
                        .password("           ")
                        .build()
        );
    }
}
