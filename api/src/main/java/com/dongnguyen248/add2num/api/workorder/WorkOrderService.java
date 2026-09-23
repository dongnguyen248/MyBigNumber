package com.dongnguyen248.add2num.api.workorder;

import java.time.Instant;

import com.dongnguyen248.add2num.api.common.InvalidStateTransitionException;
import com.dongnguyen248.add2num.api.common.ResourceNotFoundException;
import com.dongnguyen248.add2num.api.equipment.EquipmentStatus;
import com.dongnguyen248.add2num.api.equipment.EquipmentRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final EquipmentRepository equipmentRepository;

    public WorkOrderService(WorkOrderRepository workOrderRepository, EquipmentRepository equipmentRepository) {
        this.workOrderRepository = workOrderRepository;
        this.equipmentRepository = equipmentRepository;
    }

    public WorkOrderResponse create(CreateWorkOrderRequest request, String username) {
        ensureActiveEquipment(request.equipmentId());
        WorkOrder workOrder = new WorkOrder(
                request.equipmentId(),
                request.description(),
                request.priority(),
                username,
                Instant.now());
        return WorkOrderResponse.from(workOrderRepository.save(workOrder));
    }

    @Transactional(readOnly = true)
    public Page<WorkOrder> findAll(Priority priority, WorkOrderStatus status, Pageable pageable) {
        if (priority != null && status != null) {
            return workOrderRepository.findByPriorityAndStatus(priority, status, pageable);
        }
        if (priority != null) {
            return workOrderRepository.findByPriority(priority, pageable);
        }
        if (status != null) {
            return workOrderRepository.findByStatus(status, pageable);
        }
        return workOrderRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public WorkOrderResponse findById(Long id) {
        return workOrderRepository.findById(id)
                .map(WorkOrderResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Work order was not found."));
    }

    public WorkOrderResponse updateStatus(Long id, WorkOrderStatus newStatus) {
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work order was not found."));
        validateTransition(workOrder.getStatus(), newStatus);
        workOrder.updateStatus(newStatus, Instant.now());
        return WorkOrderResponse.from(workOrderRepository.save(workOrder));
    }

    private void ensureActiveEquipment(String equipmentId) {
        boolean active = equipmentRepository.existsByEquipmentIdAndStatus(equipmentId, EquipmentStatus.ACTIVE);
        if (!active) {
            throw new IllegalArgumentException("Equipment does not exist or is not active.");
        }
    }

    private void validateTransition(WorkOrderStatus currentStatus, WorkOrderStatus newStatus) {
        boolean valid = currentStatus == WorkOrderStatus.NEW && newStatus == WorkOrderStatus.IN_PROGRESS;
        valid = valid || currentStatus == WorkOrderStatus.IN_PROGRESS && newStatus == WorkOrderStatus.DONE;
        if (!valid) {
            throw new InvalidStateTransitionException("The requested status transition is not allowed.");
        }
    }
}
