package org.servicehub.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.servicehub.dto.order.OrderCreateRequest;
import org.servicehub.dto.order.OrderResponse;
import org.servicehub.dto.order.OrderUpdateRequest;
import org.servicehub.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> create(
            @Valid @RequestBody OrderCreateRequest request,
            @RequestParam("id") Long customerId
            ) {
        OrderResponse response = orderService.create(request, customerId);
        return ResponseEntity.created(URI.create("/api/order/" + response.id()))
                .body(response);
    }

    @GetMapping
    public List<OrderResponse> getAll() {
        return orderService.findAll();
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
}
