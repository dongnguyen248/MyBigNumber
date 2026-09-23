package com.dongnguyen248.add2num.api.workorder;

import java.util.List;

import org.springframework.data.domain.Page;

public record WorkOrderPageResponse(
        List<WorkOrderResponse> items,
        int page,
        int size,
        long totalItems,
        int totalPages) {

    public static WorkOrderPageResponse from(Page<WorkOrder> workOrders) {
        return new WorkOrderPageResponse(
                workOrders.getContent().stream().map(WorkOrderResponse::from).toList(),
                workOrders.getNumber(),
                workOrders.getSize(),
                workOrders.getTotalElements(),
                workOrders.getTotalPages());
    }
}
