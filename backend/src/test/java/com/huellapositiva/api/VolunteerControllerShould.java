package com.huellapositiva.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.RegisterVolunteerRequestDto;
import com.huellapositiva.domain.actions.RegisterVolunteerAction;
import com.huellapositiva.domain.exception.EmailException;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestData.class)
class VolunteerControllerShould {

    private static final String baseUri = "/api/v1/volunteers";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private TestData testData;

    @Autowired
    private MockMvc mvc;

    @SpyBean
    private RegisterVolunteerAction registerVolunteerAction;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    void registering_volunteer_should_return_201() throws Exception {
        RegisterVolunteerRequestDto dto = RegisterVolunteerRequestDto.builder()
                .email("foo@huellapositiva.com")
                .password("password")
                .build();

        String body = objectMapper.writeValueAsString(dto);
        mvc.perform(post(baseUri)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void registering_volunteer_without_password_should_return_400() throws Exception {
        RegisterVolunteerRequestDto dto = RegisterVolunteerRequestDto.builder()
                .email("foo@huellapositiva.com")
                .build();

        String body = objectMapper.writeValueAsString(dto);
        mvc.perform(post(baseUri)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registering_volunteer_with_short_password_should_return_400() throws Exception {
        String shortPassword = "12345";
        RegisterVolunteerRequestDto dto = RegisterVolunteerRequestDto.builder()
                .email("foo@huellapositiva.com")
                .password(shortPassword)
                .build();

        String body = objectMapper.writeValueAsString(dto);
        mvc.perform(post(baseUri)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registering_volunteer_without_email_should_return_400() throws Exception {
        RegisterVolunteerRequestDto dto = RegisterVolunteerRequestDto.builder()
                .password("password")
                .build();

        String body = objectMapper.writeValueAsString(dto);
        mvc.perform(post(baseUri)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registering_volunteer_null_should_return_400() throws Exception {
        mvc.perform(post(baseUri)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registering_volunteer_fail_sending_email_confirmation_should_return_500() throws Exception {
        RegisterVolunteerRequestDto dto = RegisterVolunteerRequestDto.builder()
                .email("foo@huellapositiva.com")
                .password("1234567")
                .build();
        doThrow(new EmailException()).when(registerVolunteerAction).execute(dto);

        String body = objectMapper.writeValueAsString(dto);
        mvc.perform(post(baseUri)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }


    @ParameterizedTest
    @MethodSource("provideMalformedEmails")
    void registering_volunteer_with_malformed_email_should_return_400(String malformedEmail) throws Exception {
        RegisterVolunteerRequestDto dto = RegisterVolunteerRequestDto.builder()
                .email(malformedEmail)
                .password("password")
                .build();

        String body = objectMapper.writeValueAsString(dto);
        mvc.perform(post(baseUri)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    private static Stream<String> provideMalformedEmails() {
        return Stream.of(
                ".username@yahoo.com",
                "username@yahoo.com.",
                "username@yahoo..com",
                "username@yahoo.c",
                "username@yahoo.corporate"
        );
    }

}






