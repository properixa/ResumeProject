package org.servicehub.controller;

import lombok.RequiredArgsConstructor;
import org.servicehub.dto.service.ServiceCreateRequest;
import org.servicehub.dto.service.ServiceResponse;
import org.servicehub.dto.service.ServiceUpdateRequest;
import org.servicehub.service.ServiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service")
@RequiredArgsConstructor
public class ServiceController {
    private final ServiceService service;

    @GetMapping
    public List<ServiceResponse> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ServiceResponse getById(@PathVariable("id") Long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceResponse> update(
            @PathVariable("id") Long id,
            @RequestBody ServiceUpdateRequest request
            ) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PostMapping
    public ResponseEntity<ServiceResponse> create(
            @RequestBody ServiceCreateRequest request,
            @RequestParam("id") Long executorId
            ) {
        return ResponseEntity.ok(service.create(request, executorId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @PathVariable("id") Long id
    ) {
        service.remove(id);
        return ResponseEntity.noContent().build();
    }
}
