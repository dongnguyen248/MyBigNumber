package com.dongnguyen248.add2num.api.workorder;

import java.time.Instant;

public record WorkOrderResponse(
        Long id,
        String equipmentId,
        String description,
        Priority priority,
        WorkOrderStatus status,
        String createdBy,
        Instant createdAt,
        Instant updatedAt) {

    public static WorkOrderResponse from(WorkOrder workOrder) {
        return new WorkOrderResponse(
                workOrder.getId(),
                workOrder.getEquipmentId(),
                workOrder.getDescription(),
                workOrder.getPriority(),
                workOrder.getStatus(),
                workOrder.getCreatedBy(),
                workOrder.getCreatedAt(),
                workOrder.getUpdatedAt());
    }
}
