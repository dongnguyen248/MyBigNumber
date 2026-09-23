package com.dongnguyen248.add2num.api.workorder;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {

    Page<WorkOrder> findByPriorityAndStatus(Priority priority, WorkOrderStatus status, Pageable pageable);

    Page<WorkOrder> findByPriority(Priority priority, Pageable pageable);

    Page<WorkOrder> findByStatus(WorkOrderStatus status, Pageable pageable);
}
