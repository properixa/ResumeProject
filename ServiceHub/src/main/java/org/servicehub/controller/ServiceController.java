package org.servicehub.controller;

import lombok.RequiredArgsConstructor;
import org.servicehub.dto.auth.UserPrincipal;
import org.servicehub.dto.service.ServiceCreateRequest;
import org.servicehub.dto.service.ServiceResponse;
import org.servicehub.dto.service.ServiceUpdateRequest;
import org.servicehub.service.ServiceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/service")
@RequiredArgsConstructor
public class ServiceController {
    private final ServiceService service;

    @GetMapping
    public Page<ServiceResponse> getAll(@RequestParam(value = "executorId", required = false) Long executorId,
                                        @PageableDefault Pageable pageable) {
        if (executorId != null) {
            return service.findAllByExecutorId(executorId, pageable);
        }
        return service.findAll(pageable);
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
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        ServiceResponse response = service.create(request, principal.id());
        return ResponseEntity.created(URI.create("/api/service/" + response.id()))
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @PathVariable("id") Long id
    ) {
        service.remove(id);
        return ResponseEntity.noContent().build();
    }
}
