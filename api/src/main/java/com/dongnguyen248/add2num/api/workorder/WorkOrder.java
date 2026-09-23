package com.dongnguyen248.add2num.api.workorder;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "work_orders")
public class WorkOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String equipmentId;

    private String description;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    private WorkOrderStatus status;

    private String createdBy;
    private Instant createdAt;
    private Instant updatedAt;

    protected WorkOrder() {
    }

    public WorkOrder(String equipmentId, String description, Priority priority, String createdBy, Instant createdAt) {
        this.equipmentId = equipmentId;
        this.description = description;
        this.priority = priority;
        this.status = WorkOrderStatus.NEW;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public void updateStatus(WorkOrderStatus newStatus, Instant updatedAt) {
        this.status = newStatus;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public String getDescription() {
        return description;
    }

    public Priority getPriority() {
        return priority;
    }

    public WorkOrderStatus getStatus() {
        return status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
