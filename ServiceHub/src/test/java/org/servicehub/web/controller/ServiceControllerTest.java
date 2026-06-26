package org.servicehub.web.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.servicehub.dto.auth.UserPrincipal;
import org.servicehub.dto.service.ServiceCreateRequest;
import org.servicehub.dto.service.ServiceResponse;
import org.servicehub.dto.service.ServiceUpdateRequest;
import org.servicehub.exception.exception.service.UserServiceNotFoundException;
import org.servicehub.exception.exception.user.InvalidRoleException;
import org.servicehub.exception.exception.user.UserNotFoundException;
import org.servicehub.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ServiceControllerTest extends AbstractControllerTest {

    @Autowired
    private ServiceService serviceService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void getAll_shouldReturnList() throws Exception {
        List<ServiceResponse> response = Arrays.asList(
                new ServiceResponse(1L, "test", "test", 1L, "name name"),
                new ServiceResponse(2L, "test", "test", 2L, "name name")
        );

        Mockito.when(serviceService.findAll())
                .thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/service"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("test"));
    }

    @Test
    void getAll_shouldReturn403_whenForbidden() throws Exception {
        Mockito.when(serviceService.findAll())
                .thenReturn(List.of());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/service"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void getById_shouldReturn_whenValid() throws Exception {
        Long serviceId = 1L;
        ServiceResponse response = new ServiceResponse(1L, "test", "test", 1L, "name name");

        Mockito.when(serviceService.findById(serviceId))
                .thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/service/{id}", serviceId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(serviceId))
                .andExpect(jsonPath("$.executorId").value(1L));
    }

    @Test
    @WithMockUser
    void update_shouldReturn_withValid() throws Exception {
        Long serviceId = 1L;
        ServiceUpdateRequest request = new ServiceUpdateRequest("new title", "new description", 1L);
        ServiceResponse response = new ServiceResponse(1L, "new title", "new description",
                1L, "full name");

        Mockito.when(serviceService.update(serviceId, request))
                .thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/service/{id}", serviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("new title"));
    }

    @Test
    @WithMockUser
    void update_shouldReturn404_whenServiceNotFound() throws Exception {
        Long serviceId = 1L;
        ServiceUpdateRequest request = new ServiceUpdateRequest("", "", 1L);

        Mockito.when(serviceService.update(serviceId, request))
                .thenThrow(new UserServiceNotFoundException("ex"));

        mockMvc.perform(MockMvcRequestBuilders.put("/api/service/{id}", serviceId)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void update_shouldReturn404_whenUserNotFound() throws Exception {
        Long serviceId = 1L;
        ServiceUpdateRequest request = new ServiceUpdateRequest("", "", 2L);

        Mockito.when(serviceService.update(serviceId, request))
                .thenThrow(new UserNotFoundException("ex"));

        mockMvc.perform(MockMvcRequestBuilders.put("/api/service/{id}", serviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void update_shouldReturn400_whenRoleNotExecutor() throws Exception {
        Long serviceId = 1L;
        ServiceUpdateRequest request = new ServiceUpdateRequest("", "", 1L);

        Mockito.doThrow(new InvalidRoleException("ex")).when(serviceService).update(serviceId, request);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/service/{id}", serviceId)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn() throws Exception {
        UserPrincipal principal = new UserPrincipal(1L, "email");
        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        Set.of()
                );
        Long executorId = 1L;
        ServiceCreateRequest request = new ServiceCreateRequest("title", "desc");
        ServiceResponse response = new ServiceResponse(1L, "title", "desc",
                1L, "name name");

        Mockito.when(serviceService.create(request, executorId))
                .thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/service")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth))
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void create_shouldThrow404_whenExecutorNotFound() throws Exception {
        UserPrincipal principal = new UserPrincipal(1L, "email");
        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        null
                );
        ServiceCreateRequest request = new ServiceCreateRequest("test", "test");

        Mockito.when(serviceService.create(request, 1L))
                .thenThrow(new UserNotFoundException("ex"));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/service")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void remove_shouldRemove() throws Exception {
        Long serviceId = 1L;

        Mockito.doNothing().when(serviceService).remove(serviceId);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/service/{id}", serviceId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void remove_shouldReturn404_whenServiceNotFound() throws Exception {
        Long serviceId = 1L;

        Mockito.doThrow(new UserServiceNotFoundException("ex")).when(serviceService).remove(serviceId);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/service/{id}", serviceId))
                .andExpect(status().isNotFound());
    }
}
