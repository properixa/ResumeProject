package org.servicehub.web.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.servicehub.dto.auth.UserPrincipal;
import org.servicehub.dto.filter.OrderFilter;
import org.servicehub.dto.order.OrderCreateRequest;
import org.servicehub.dto.order.OrderResponse;
import org.servicehub.dto.order.OrderUpdateRequest;
import org.servicehub.entity.enums.OrderStatus;
import org.servicehub.exception.exception.order.InvalidForServiceExecutorException;
import org.servicehub.exception.exception.order.OrderChangeStatusException;
import org.servicehub.exception.exception.order.OrderNotFoundException;
import org.servicehub.exception.exception.order.OrderStatusNotFoundException;
import org.servicehub.exception.exception.user.UserNotFoundException;
import org.servicehub.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class OrderControllerTest extends AbstractControllerTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void getAll_shouldReturnList() throws Exception {
        OrderFilter filter = new OrderFilter(null, null);
        List<OrderResponse> responses = List.of(
                new OrderResponse(1L, 1L, 1L, 1L, "test", "NEW"),
                new OrderResponse(2L, 1L, 2L, 2L, "test", "NEW")
        );
        Page<OrderResponse> page = new PageImpl<>(responses, Pageable.ofSize(10), 10);

        when(orderService.findAll(any(OrderFilter.class), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/order"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    void getAll_shouldThrow403_whenUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/order"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void getById_shouldReturn_whenValid() throws Exception {
        Long orderId = 1L;
        OrderResponse response = new OrderResponse(1L, 1L, 1L, 1L, "test", "test");

        when(orderService.findById(orderId))
                .thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/order/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(orderId));
    }

    @Test
    @WithMockUser
    void getById_shouldThrow_whenOrderNotFound() throws Exception {
        Mockito.doThrow(new OrderNotFoundException("ex"))
                .when(orderService).findById(1L);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/order/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void update_shouldReturn_whenUpdated() throws Exception {
        Long orderId = 3L;
        OrderUpdateRequest request = new OrderUpdateRequest(1L, 1L, "test", "test");
        OrderResponse response = new OrderResponse(orderId, 1L, 1L, 1L, "test", "test");

        when(orderService.update(orderId, request))
                .thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/order/{id}", orderId)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(orderId));
    }

    @Test
    @WithMockUser
    void update_shouldThrow404_whenOrderNotFound() throws Exception {
        Long orderId = 1L;
        OrderUpdateRequest request = new OrderUpdateRequest(1L, 1L, "test", "test");

        Mockito.doThrow(new OrderNotFoundException("ex"))
                .when(orderService).update(orderId, request);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/order/{id}", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void update_shouldThrow400_whenExecutorOrServiceIsNull() throws Exception {
        Long orderId = 1L;
        OrderUpdateRequest request = new OrderUpdateRequest(1L, 1L, "test", "test");

        Mockito.doThrow(IllegalArgumentException.class)
                .when(orderService).update(orderId, request);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/order/{id}", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void update_shouldThrow409_whenInvalidServiceForExecutor() throws Exception {
        Long orderId = 1L;
        OrderUpdateRequest request = new OrderUpdateRequest(1L, 1L, "test", "test");

        Mockito.doThrow(InvalidForServiceExecutorException.class)
                .when(orderService).update(orderId, request);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/order/{id}", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void update_shouldThrow400_whenOrderStatusInvalid() throws Exception {
        Long orderId = 1L;
        OrderUpdateRequest request = new OrderUpdateRequest(1L, 1L, "test", "test");

        Mockito.doThrow(new OrderStatusNotFoundException("ex"))
                .when(orderService).update(orderId, request);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/order/{id}", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn_whenValid() throws Exception {
        UserPrincipal principal = new UserPrincipal(1L, "test");
        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        null
                );
        OrderCreateRequest request = new OrderCreateRequest(1L, 1L, "test");
        OrderResponse response = new OrderResponse(1L, 1L, 1L, 1L, "test", "NEW");

        when(orderService.create(request, principal))
                .thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/order")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void create_shouldReturn404_whenUserNotFound() throws Exception {
        UserPrincipal principal = new UserPrincipal(1L, "test");
        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        null
                );
        OrderCreateRequest request = new OrderCreateRequest(1L, 1L, "test");

        Mockito.doThrow(new UserNotFoundException("ex"))
                .when(orderService).create(request, principal);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/order")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth))
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void remove_shouldRemove() throws Exception {
        Long orderId = 1L;

        Mockito.doNothing().when(orderService).remove(orderId);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/order/{id}", orderId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void remove_shouldThrow404_whenOrderNotFound() throws Exception {
        Long orderId = 1L;

        Mockito.doThrow(OrderNotFoundException.class)
                .when(orderService).remove(orderId);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/order/{id}", orderId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void updateStatus_shouldReturnResponse_whenAllValid() throws Exception {
        Long orderId = 1L;
        String newStatus = "ACCEPTED";
        OrderResponse response = new OrderResponse(orderId, 1L, 1L, 1L, "", "");

        when(orderService.updateStatus(orderId, newStatus))
                .thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/order/{id}/{status}", orderId, newStatus))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId));
    }

    @Test
    @WithMockUser
    void updateStatus_shouldReturn404_whenOrderNotFound() throws Exception {
        Long orderId = 1L;
        String newStatus = "NEW";

        when(orderService.updateStatus(orderId, newStatus))
                .thenThrow(OrderNotFoundException.class);

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/order/{id}/{status}", orderId, newStatus))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void updateStatus_shouldReturn400_whenOrderStatusInvalid() throws Exception {
        Long orderId = 1L;
        String newStatus = "INVALID";

        when(orderService.updateStatus(orderId, newStatus))
                .thenThrow(OrderStatusNotFoundException.class);

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/order/{id}/{status}", orderId, newStatus))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void updateStatus_shouldReturn400_whenOrderStatusTransitionInvalid() throws Exception {
        Long orderId = 1L;
        String newStatus = "STATUS";

        when(orderService.updateStatus(orderId, newStatus))
                .thenThrow(OrderChangeStatusException.class);

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/order/{id}/{status}", orderId, newStatus))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getAll_shouldReturnWithValidPage() throws Exception {
        List<OrderResponse> responses = List.of(
                new OrderResponse(1L, 1L, 1L, 1L, "test", "NEW"),
                new OrderResponse(2L, 1L, 2L, 2L, "test", "NEW")
        );
        Pageable pageable = PageRequest.of(1, 5, Sort.by("id").ascending());
        Page<OrderResponse> page = new PageImpl<>(responses, pageable, 10);
        OrderFilter filter = new OrderFilter(null, OrderStatus.NEW);

        when(orderService.findAll(any(OrderFilter.class), any(Pageable.class))).thenReturn(page);
        mockMvc.perform(MockMvcRequestBuilders.get("/api/order")
                        .param("status", filter.status().toString())
                        .param("page", String.valueOf(pageable.getPageNumber()))
                        .param("size", String.valueOf(pageable.getPageSize()))
                        .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.totalElements").value(10))
                .andExpect(jsonPath("$.totalPages").value(2)) // 10 элементов / 5 на страницу = 2
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.sort.sorted").value(true));
    }
}
