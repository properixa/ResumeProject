package org.servicehub.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.servicehub.dto.auth.UserPrincipal;
import org.servicehub.dto.filter.OrderFilter;
import org.servicehub.dto.order.OrderCreateRequest;
import org.servicehub.dto.order.OrderResponse;
import org.servicehub.dto.order.OrderUpdateRequest;
import org.servicehub.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> create(
            @Valid @RequestBody OrderCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        OrderResponse response = orderService.create(request, principal);
        return ResponseEntity.created(URI.create("/api/order/" + response.id()))
                .body(response);
    }

    @GetMapping
    public Page<OrderResponse> getAll(@ModelAttribute OrderFilter filter,
                                      @PageableDefault Pageable pageable) {
        return orderService.findAll(filter, pageable);
    }

    @GetMapping("/{id}")
    public OrderResponse getById(@PathVariable("id") Long id) {
        return orderService.findById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> update(@PathVariable("id") Long id,
                                                @RequestBody OrderUpdateRequest request) {
        return ResponseEntity.ok(orderService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        orderService.remove(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/{status}")
    public OrderResponse updateStatus(@PathVariable("id") Long id,
                                      @PathVariable("status") @NotNull String status) {
        return orderService.updateStatus(id, status.toUpperCase());
    }
}
