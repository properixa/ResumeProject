package org.servicehub.web.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.servicehub.dto.auth.LoginRequest;
import org.servicehub.dto.user.UserCreateRequest;
import org.servicehub.dto.user.UserResponse;
import org.servicehub.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthControllerTest extends AbstractControllerTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void register_shouldReturn_whenRegister() throws Exception {
        UserCreateRequest request = new UserCreateRequest("name name", "pass", "email@email", "222222222");
        UserResponse response = new UserResponse(1L, "name name", "email@email", "222222222");

        Mockito.when(authService.register(request))
                .thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser
    void login_shouldReturnToken() throws Exception {
        LoginRequest request = new LoginRequest("email", "password");
        String token = "token";

        Mockito.when(authService.getToken(request))
                .thenReturn(token);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value(token));
    }
}
