package com.huellapositiva.util;

import com.huellapositiva.domain.Roles;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.Collections;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public final class TestUtils {

    public static UserDetails withMockUser(Roles role) {
        return new User("foo@huellapositiva.com", "pass", Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role)));
    }

    public static UserDetails withMockUser(String username, Roles role) {
        return new User(username, "pass", Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role)));
    }

    public static void authenticatedPostRequest(MockMvc mvc, String path, Roles role, String body, ResultMatcher expectedResult, ResultMatcher expectedHeader) throws Exception {
        mvc.perform(post(path)
                .with(user(withMockUser(role)))
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(expectedResult)
                .andExpect(expectedHeader);
    }

    public static void anonymousPostRequest(MockMvc mvc, String path, String body, ResultMatcher expectedResult, ResultMatcher expectedHeader) throws Exception {
        mvc.perform(post(path)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(expectedResult)
                .andExpect(expectedHeader);
    }
}
