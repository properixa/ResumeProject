package org.servicehub.web.controller;

import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.servicehub.dto.filter.UserFilter;
import org.servicehub.dto.user.UserResponse;
import org.servicehub.dto.user.UserUpdateRequest;
import org.servicehub.exception.exception.user.DuplicateEmailException;
import org.servicehub.exception.exception.user.InvalidRoleException;
import org.servicehub.exception.exception.user.UserNotFoundException;
import org.servicehub.service.ServicehubUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class UserControllerTest extends AbstractControllerTest {

    @Autowired
    private ServicehubUserService userService;

    @Autowired
    private Validator validator;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void getAll_shouldReturn() throws Exception {
        List<UserResponse> mockUsers = Arrays.asList(
                new UserResponse(1L, "name name name", "email", "phone"),
                new UserResponse(2L, "name name name", "email2", "phone2")
        );
        Page<UserResponse> page = new PageImpl<>(mockUsers, Pageable.ofSize(20), mockUsers.size());
        when(userService.getAll(any(UserFilter.class), any(Pageable.class))).thenReturn(page);
        mockMvc.perform(MockMvcRequestBuilders.get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].fullName").value("name name name"));
    }

    @Test
    void getAll_shouldReturn403_whenForbidden() throws Exception {
        when(userService.getAll(any(UserFilter.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void getById_shouldReturnUser_whenExists() throws Exception {
        UserResponse mockUser = new UserResponse(1L, "name", "test", "test");
        Mockito.when(userService.getById(1L))
                .thenReturn(mockUser);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.fullName").value("name"));
    }

    @Test
    @WithMockUser
    void getById_shouldReturn404_whenServiceThrowsException() throws Exception {
        Long userId = 999L;
        Mockito.when(userService.getById(userId))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/{id}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void updateUser_shouldReturnUpdated_whenValidRequest() throws Exception {
        Long userId = 1L;
        UserUpdateRequest request = new UserUpdateRequest("new Name", "newPass",
                "newEmail@email", "1232123", Set.of());
        UserResponse updatedUser = new UserResponse(userId, "new Name", "newEmail@email", "1232123");

        Mockito.when(userService.update(userId, request)).thenReturn(updatedUser);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.fullName").value("new Name"));
    }

    @Test
    @WithMockUser
    void updateUser_shouldReturn404_whenUserNotFound() throws Exception {
        Long userId = 1L;
        UserUpdateRequest request = new UserUpdateRequest("name name",
                "1", "email@email", "1232322", null);

        doThrow(new UserNotFoundException("ex")).when(userService).update(userId, request);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void updateUser_shouldReturn409_whenDuplicatesEmail() throws Exception {
        Long userId = 1L;
        UserUpdateRequest request = new UserUpdateRequest("name name",
                "1", "email@email", "7232323", null);
        doThrow(new DuplicateEmailException("ex")).when(userService).update(userId, request);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void updateUser_shouldReturn400_whenInvalidRole() throws Exception {
        Long userId = 1L;
        UserUpdateRequest request = new UserUpdateRequest("1", "1", "1", "1", null);
        Mockito.when(userService.update(userId, request))
                .thenThrow(new InvalidRoleException("ex"));

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void remove_shouldRemove() throws Exception {
        Long userId = 1L;
        doNothing().when(userService).remove(userId);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/users/{id}", userId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void remove_shouldReturn404_whenUserNotFound() throws Exception {
        Long userId = 1L;
        doThrow(new UserNotFoundException("ex")).when(userService).remove(userId);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/users/{id}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void update_shouldReturn400_whenInvalidRequest() throws Exception {
        Long userId = 1L;
        UserUpdateRequest request = new UserUpdateRequest("", "", "", "", null);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/{id}", userId)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

}
