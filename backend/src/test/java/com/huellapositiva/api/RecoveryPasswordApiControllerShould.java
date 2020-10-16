package com.huellapositiva.api;

import com.huellapositiva.infrastructure.orm.entities.JpaCredential;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static com.huellapositiva.domain.model.valueobjects.Roles.VOLUNTEER;
import static com.huellapositiva.domain.model.valueobjects.Roles.VOLUNTEER_NOT_CONFIRMED;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RecoveryPasswordApiControllerShould {
    private static final String baseUri = "/api/v1/restore-password";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TestData testData;

    @Test
    void return_200_when_sends_an_email() throws Exception{

       /* UUID token = UUID.randomUUID();
        JpaCredential jpaCredential = testData.createCredential("email@huellapositiva.com", token, "password", VOLUNTEER);

        ResultActions result = mvc.perform(get(baseUri + '/' + jpaCredential.getEmail())
                .contentType(APPLICATION_JSON))
                .andExpect(status().isNoContent());*/
    }
}