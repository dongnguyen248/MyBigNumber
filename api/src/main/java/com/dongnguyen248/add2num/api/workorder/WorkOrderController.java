package com.dongnguyen248.add2num.api.workorder;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    public WorkOrderController(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    @PostMapping
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<WorkOrderResponse> create(
            @Valid @RequestBody CreateWorkOrderRequest request,
            Authentication authentication) {
        WorkOrderResponse response = workOrderService.create(request, authentication.getName());
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'SUPERVISOR')")
    public WorkOrderPageResponse findAll(
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) WorkOrderStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return WorkOrderPageResponse.from(workOrderService.findAll(priority, status, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'SUPERVISOR')")
    public WorkOrderResponse findById(@PathVariable Long id) {
        return workOrderService.findById(id);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public WorkOrderResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWorkOrderStatusRequest request) {
        return workOrderService.updateStatus(id, request.status());
    }
}
